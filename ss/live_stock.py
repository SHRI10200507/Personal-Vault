from datetime import datetime
from pathlib import Path
import time

import pythoncom
import pywintypes
import requests
import win32com.client as win32
import yfinance as yf


base_path = Path(__file__).resolve().parent
file_path = base_path / "live_stock.xlsx"
env_path = base_path / ".env"
default_update_interval_seconds = 10
pb_cache_seconds = 3600
max_rows = 100
max_columns = 50
input_headers = {"typestocksymbol", "stock", "stocksymbol", "symbol", "nsecode"}
main_headers = [
    "Type Stock Symbol",
    "Verification",
    "NSE Symbol",
    "Price",
    "Day High",
    "Day Low",
    "Day Change %",
    "PE Ratio",
    "PB Ratio",
    "52W Low",
    "52W High",
    "Company",
    "Sector",
    "Updated At",
    "Status",
]
output_fields = {
    "nsesymbol": "symbol",
    "nsecode": "symbol",
    "price": "price",
    "ltp": "price",
    "dayhigh": "day_high",
    "intradayhigh": "day_high",
    "high": "day_high",
    "daylow": "day_low",
    "intradaylow": "day_low",
    "low": "day_low",
    "daychangepercent": "percent_change",
    "daychangepercentage": "percent_change",
    "change": "change",
    "percentchange": "percent_change",
    "changepercent": "percent_change",
    "pe": "pe_ratio",
    "peratio": "pe_ratio",
    "pb": "pb_ratio",
    "pbratio": "pb_ratio",
    "52wlow": "week_low",
    "52weeklow": "week_low",
    "52whigh": "week_high",
    "52weekhigh": "week_high",
    "company": "company",
    "companyname": "company",
    "sector": "sector",
    "sectorname": "sector",
    "updatedat": "updated_at",
    "time": "updated_at",
    "status": "status",
}
verification_headers = {"verification", "verify"}
nse_base_url = "https://www.nseindia.com"
nse_quote_url = f"{nse_base_url}/api/quote-equity"
nse_search_url = f"{nse_base_url}/api/search/autocomplete"
nse_headers = {
    "accept": "application/json,text/plain,*/*",
    "accept-language": "en-US,en;q=0.9",
    "referer": f"{nse_base_url}/get-quotes/equity",
    "user-agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    ),
}
pb_ratio_cache = {}
symbol_cache = {}


class SymbolResolutionError(Exception):
    pass


def get_excel_workbook(path):
    pythoncom.CoInitialize()

    try:
        excel = win32.GetActiveObject("Excel.Application")
    except pywintypes.com_error:
        excel = win32.DispatchEx("Excel.Application")

    excel.Visible = True

    full_path = str(path)
    for workbook in excel.Workbooks:
        if workbook.FullName.lower() == full_path.lower():
            return excel, workbook

    if path.exists():
        workbook = excel.Workbooks.Open(full_path)
    else:
        workbook = excel.Workbooks.Add()
        workbook.SaveAs(full_path)

    return excel, workbook


def load_env(path):
    values = {}

    if not path.exists():
        return values

    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()

        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", 1)
        values[key.strip()] = value.strip().strip('"').strip("'")

    return values


def get_fetch_interval_seconds():
    active_env_path = Path.cwd() / ".env"

    if not active_env_path.exists():
        active_env_path = env_path

    env_values = load_env(active_env_path)
    raw_value = env_values.get("FETCH_INTERVAL_SECONDS", default_update_interval_seconds)

    try:
        interval = int(raw_value)
    except (TypeError, ValueError):
        return default_update_interval_seconds

    return max(1, interval)


def create_nse_session():
    session = requests.Session()
    session.headers.update(nse_headers)
    session.get(nse_base_url, timeout=10)
    return session


def get_nse_quote(session, symbol):
    response = request_nse(session, nse_quote_url, {"symbol": symbol})
    data = response.json()
    metadata = data.get("metadata", {})
    price_info = data.get("priceInfo", {})
    industry_info = data.get("industryInfo", {})
    intraday_high_low = price_info.get("intraDayHighLow", {})
    week_high_low = price_info.get("weekHighLow", {})

    pb_ratio = find_first_number(data, ["pdSymbolPb", "pbRatio", "priceToBook", "priceBookValue"])

    if pb_ratio is None:
        pb_ratio = get_pb_ratio_from_yahoo(symbol)

    return {
        "symbol": data.get("info", {}).get("symbol", symbol),
        "company": data.get("info", {}).get("companyName", ""),
        "sector": industry_info.get("sector") or metadata.get("industry") or "",
        "price": to_number(price_info.get("lastPrice")),
        "day_high": to_number(intraday_high_low.get("max")),
        "day_low": to_number(intraday_high_low.get("min")),
        "change": to_number(price_info.get("change")),
        "percent_change": to_number(price_info.get("pChange")),
        "pe_ratio": to_number(metadata.get("pdSymbolPe")),
        "pb_ratio": pb_ratio,
        "week_low": to_number(week_high_low.get("min")),
        "week_high": to_number(week_high_low.get("max")),
    }


def resolve_nse_symbol(session, user_value):
    cleaned_value = clean_stock_symbol(user_value)

    if not cleaned_value:
        return None

    if cleaned_value in symbol_cache:
        return symbol_cache[cleaned_value]

    if is_direct_symbol(cleaned_value) and nse_symbol_exists(session, cleaned_value):
        symbol_cache[cleaned_value] = cleaned_value
        return cleaned_value

    response = request_nse(session, nse_search_url, {"q": cleaned_value})
    data = response.json()

    for result in get_search_results(data):
        symbol = result.get("symbol") or result.get("symbol_info")

        if symbol and nse_symbol_exists(session, symbol):
            resolved_symbol = clean_stock_symbol(symbol)
            symbol_cache[cleaned_value] = resolved_symbol
            return resolved_symbol

    raise SymbolResolutionError(f"Could not find NSE symbol for '{user_value}'")


def nse_symbol_exists(session, symbol):
    try:
        response = request_nse(session, nse_quote_url, {"symbol": clean_stock_symbol(symbol)})
        return bool(response.json().get("info", {}).get("symbol"))
    except Exception:
        return False


def request_nse(session, url, params):
    response = session.get(url, params=params, timeout=10)

    if response.status_code in (401, 403):
        session.get(nse_base_url, timeout=10)
        response = session.get(url, params=params, timeout=10)

    response.raise_for_status()
    return response


def get_search_results(data):
    if isinstance(data, list):
        return [item for item in data if isinstance(item, dict)]

    if not isinstance(data, dict):
        return []

    results = []

    for key in ("symbols", "result", "data"):
        value = data.get(key)

        if isinstance(value, list):
            results.extend(item for item in value if isinstance(item, dict))

    return results


def is_direct_symbol(value):
    allowed_characters = set("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789&-")
    return len(value) <= 20 and all(character in allowed_characters for character in value)


def to_number(value):
    if value is None or value == "":
        return None

    if isinstance(value, (int, float)):
        return value

    try:
        return float(str(value).replace(",", ""))
    except ValueError:
        return None


def find_first_number(data, keys):
    if isinstance(data, dict):
        for key, value in data.items():
            if key in keys:
                number = to_number(value)

                if number is not None:
                    return number

            found = find_first_number(value, keys)

            if found is not None:
                return found

    if isinstance(data, list):
        for item in data:
            found = find_first_number(item, keys)

            if found is not None:
                return found

    return None


def get_pb_ratio_from_yahoo(symbol):
    cached_value = pb_ratio_cache.get(symbol)
    now = time.time()

    if cached_value and now - cached_value["time"] < pb_cache_seconds:
        return cached_value["value"]

    try:
        stock = yf.Ticker(f"{symbol}.NS")
        pb_ratio = to_number(stock.info.get("priceToBook"))
        pb_ratio_cache[symbol] = {"value": pb_ratio, "time": now}
        return pb_ratio
    except Exception:
        return None


def clean_stock_symbol(user_value):
    if user_value is None:
        return None

    typed_value = str(user_value).strip().upper()

    if not typed_value or typed_value == "NONE":
        return None

    return typed_value.replace(".NS", "")


def is_stock_symbol(value):
    symbol = clean_stock_symbol(value)

    if not symbol:
        return False

    if symbol in {"N/A", "OK", "ERROR"}:
        return False

    return True


def normalize_header(value):
    if value is None:
        return ""

    normalized = str(value).lower().replace("%", "percent")
    return "".join(character for character in normalized if character.isalnum())


def get_header_columns(sheet):
    stock_columns = []
    verification_column = None
    data_columns = {}

    for column in range(1, max_columns + 1):
        header_key = normalize_header(sheet.Cells(1, column).Value)

        if not header_key:
            continue

        if header_key in input_headers:
            stock_columns.append(column)

        if header_key in verification_headers:
            verification_column = column

        if header_key in output_fields:
            data_columns[column] = output_fields[header_key]

    return stock_columns, verification_column, data_columns


def repair_sheet_layout(sheet):
    stock_values = []
    verification_values = []

    for row in range(2, max_rows + 1):
        stock_value = sheet.Cells(row, 1).Value
        verification_value = sheet.Cells(row, 2).Value
        stock_values.append(stock_value if clean_stock_symbol(stock_value) else None)
        verification_values.append(verification_value if clean_stock_symbol(verification_value) else None)

    sheet.Range(sheet.Cells(1, 1), sheet.Cells(max_rows, max_columns)).ClearContents()

    for column, header in enumerate(main_headers, start=1):
        sheet.Cells(1, column).Value = header

    for row, value in enumerate(stock_values, start=2):
        if value:
            sheet.Cells(row, 1).Value = value

    for row, value in enumerate(verification_values, start=2):
        if value:
            sheet.Cells(row, 2).Value = value


def format_data_columns(sheet, data_columns):
    numeric_fields = {
        "price",
        "day_high",
        "day_low",
        "change",
        "percent_change",
        "pe_ratio",
        "pb_ratio",
        "week_low",
        "week_high",
    }

    for column, field in data_columns.items():
        if field in numeric_fields:
            sheet.Range(sheet.Cells(2, column), sheet.Cells(max_rows, column)).NumberFormat = "0.00"
        elif field == "updated_at":
            sheet.Range(sheet.Cells(2, column), sheet.Cells(max_rows, column)).NumberFormat = "hh:mm:ss"


def clear_old_no_match_cells(sheet):
    for row in range(2, max_rows + 1):
        for column in range(1, max_columns + 1):
            if str(sheet.Cells(row, column).Value).strip().lower() == "no match":
                sheet.Cells(row, column).ClearContents()


def write_quote_to_row(sheet, row, data_columns, quote):
    values = {
        **quote,
        "updated_at": datetime.now().strftime("%H:%M:%S"),
        "status": "OK" if quote["price"] is not None else "No price found",
    }

    for column, field in data_columns.items():
        sheet.Cells(row, column).Value = values.get(field, "N/A")


def write_error_to_row(sheet, row, data_columns, error):
    now = datetime.now().strftime("%H:%M:%S")

    for column, field in data_columns.items():
        if field == "updated_at":
            sheet.Cells(row, column).Value = now
        elif field == "status":
            sheet.Cells(row, column).Value = f"Error: {error}"


def write_status_to_row(sheet, row, data_columns, message):
    now = datetime.now().strftime("%H:%M:%S")

    clear_row_data(sheet, row, data_columns)

    for column, field in data_columns.items():
        if field == "updated_at":
            sheet.Cells(row, column).Value = now
        elif field == "status":
            sheet.Cells(row, column).Value = message


def update_sheet(sheet, nse_session):
    clear_old_no_match_cells(sheet)
    stock_columns, verification_column, data_columns = get_header_columns(sheet)
    format_data_columns(sheet, data_columns)
    updated_stocks = 0

    if not stock_columns:
        print("No stock input column found. Header should be 'Type Stock Symbol' in column A.")
        return updated_stocks

    if not data_columns:
        print("No output columns found. Header row is missing NSE Symbol, Price, PE Ratio, etc.")
        return updated_stocks

    if verification_column is None:
        print("No verification column found. Header should be 'Verification' in column B.")
        return updated_stocks

    for column in stock_columns:
        for row in range(2, max_rows + 1):
            user_stock = sheet.Cells(row, column).Value
            verification_stock = sheet.Cells(row, verification_column).Value

            if user_stock is None or not str(user_stock).strip():
                continue

            if verification_stock is None or not str(verification_stock).strip():
                clear_row_data(sheet, row, data_columns)
                continue

            if not is_stock_symbol(user_stock):
                clear_row_data(sheet, row, data_columns)
                continue

            try:
                symbol = resolve_nse_symbol(nse_session, user_stock)
                verification_symbol = resolve_nse_symbol(nse_session, verification_stock)
            except SymbolResolutionError as error:
                write_status_to_row(sheet, row, data_columns, str(error))
                continue

            if not symbol or symbol != verification_symbol:
                write_status_to_row(
                    sheet,
                    row,
                    data_columns,
                    f"Verification mismatch: {symbol} != {verification_symbol}",
                )
                continue

            try:
                quote = get_nse_quote(nse_session, symbol)
                write_quote_to_row(sheet, row, data_columns, quote)
                updated_stocks += 1
            except Exception as error:
                write_error_to_row(sheet, row, data_columns, error)

    sheet.Columns("A:AZ").AutoFit()
    return updated_stocks


def clear_row_data(sheet, row, data_columns):
    for column in data_columns:
        sheet.Cells(row, column).ClearContents()


def main():
    try:
        excel, workbook = get_excel_workbook(file_path)
    except pywintypes.com_error as error:
        print("Could not start Excel automation.")
        print("Please open Excel once manually, then run this script again from your normal terminal.")
        print(f"Details: {error}")
        return

    sheet = workbook.Worksheets(1)
    repair_sheet_layout(sheet)
    workbook.Save()

    try:
        nse_session = create_nse_session()
    except requests.RequestException as error:
        print("Could not connect to NSE.")
        print("Check your internet connection, then run the script again.")
        print(f"Details: {error}")
        return

    print("Excel is open. Verified rows refresh using FETCH_INTERVAL_SECONDS from .env. Press Ctrl + C to stop.")

    try:
        while True:
            started_at = time.time()
            updated_rows = update_sheet(sheet, nse_session)
            workbook.Save()
            interval_seconds = get_fetch_interval_seconds()
            elapsed_seconds = time.time() - started_at
            sleep_seconds = max(0, interval_seconds - elapsed_seconds)
            print(
                f"Updated {updated_rows} stocks in {elapsed_seconds:.1f}s! "
                f"Next update in {sleep_seconds:.1f}s."
            )
            time.sleep(sleep_seconds)
    except KeyboardInterrupt:
        workbook.Save()
        print("Stopped.")


if __name__ == "__main__":
    main()
