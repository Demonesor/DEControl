import requests

# === Налаштування ===
PORT = 7462
TOKEN_FILE = 'token.sec'

# === Глобальні змінні ===
TOKEN = None
SERVER_IP = None

def parse_ip_from_token(token: str) -> str:
    try:
        ip_part = token.split(":")[2]
        # Наприклад: "4563" → "192.168.5.63"
        return f"192.168.{ip_part[0]}.{ip_part[1:]}"
    except Exception:
        return "127.0.0.1"

# === Завантаження токена ===
def load_token():
    global TOKEN, SERVER_IP
    if TOKEN:
        return TOKEN
    try:
        with open(TOKEN_FILE) as f:
            TOKEN = f.read().strip()
            SERVER_IP = parse_ip_from_token(TOKEN)
            return TOKEN
    except FileNotFoundError:
        print("Файл токена не знайдено.")
        return None

# === Відправка запиту ===
def send_request(endpoint, method='POST'):
    global SERVER_IP
    token = load_token()
    if not token:
        print("Немає токена. Вихід.")
        return
    if not SERVER_IP:
        SERVER_IP = parse_ip_from_token(token)

    url = f'http://{SERVER_IP}:{PORT}/{endpoint}'
    headers = {
        'Authorization': f'Bearer {token}'
    }

    try:
        if method == 'GET':
            response = requests.get(url, headers=headers)
        else:
            response = requests.post(url, headers=headers)
        
        print(f"[{endpoint.upper()}] → {response.status_code}")
        print(response.json())
    except Exception as e:
        print(f"Помилка: {e}")

# === Меню ===
def menu():
    global TOKEN, SERVER_IP
    while True:
        print("\n[1] Status")
        print("[2] Lock PC")
        print("[3] Reboot PC")
        print("[4] Shutdown PC")
        print("[5] Ввести токен вручну")
        print("[0] Вихід")
        choice = input("Вибір >> ")

        if choice == '1':
            send_request("status", method="GET")
        elif choice == '2':
            send_request("lock")
        elif choice == '3':
            send_request("reboot")
        elif choice == '4':
            send_request("shutdown")
        elif choice == '5':
            TOKEN = input("Введіть токен: ").strip()
            SERVER_IP = parse_ip_from_token(TOKEN)
            print(f"IP встановлено: {SERVER_IP}")
        elif choice == '0':
            break
        else:
            print("Невірний вибір.")

if __name__ == '__main__':
    menu()
