import os
import random
import string
import socket
from flask import Flask, request, jsonify, abort

app = Flask(__name__)


def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    ips = ""
    try:
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        ips = ip.split(".")[2]
        ips = ips +  ip.split(".")[3]
        
    except Exception:
        ip = "127.0.0.1"
        ips = ip.split(".")[2] + ip.split(".")[3]
    finally:
        s.close()
    return ips


def generate_token():
    parts = [
        ''.join(random.choices(string.ascii_uppercase + string.digits, k=3)),
        ''.join(random.choices(string.ascii_uppercase + string.digits, k=2)),
        ''.join(get_local_ip()),
    ]
    return ':'.join(parts)

def save_token(token):
    with open('token.sec', 'w') as f:
        f.write(token)

def load_token():
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    os.path.exists('token.sec')
    token = generate_token()
    save_token(token)
    print(f"Generated token: {token}")
    
    
    return token

class APIController:
    def __init__(self, token):
        self.api_token = token

    def check_auth(self):
        token = request.headers.get('Authorization')
        if token != f"Bearer {self.api_token}":
            abort(401, description="Unauthorized")

    def shutdown(self):
        self.check_auth()
        os.system('shutdown /s /t 0')
        return jsonify({"status": "success", "message": "PC shutting down"})

    def lock(self):
        self.check_auth()
        os.system('rundll32.exe user32.dll,LockWorkStation')
        return jsonify({"status": "success", "message": "PC locked"})

    def reboot(self):
        self.check_auth()
        os.system('shutdown /r /t 0')
        return jsonify({"status": "success", "message": "PC rebooting"})

    def status(self):
        self.check_auth()
        return jsonify({"status": "online", "message": "PC is running"})

token = load_token()
api = APIController(token)

@app.route('/shutdown', methods=['POST'])
def shutdown_route():
    return api.shutdown()

@app.route('/lock', methods=['POST'])
def lock_route():
    return api.lock()

@app.route('/reboot', methods=['POST'])
def reboot_route():
    return api.reboot()

@app.route('/status', methods=['GET'])
def status_route():
    return api.status()

if __name__ == '__main__':
    print(f"Using token: {token}")
    app.run(host='0.0.0.0', port=7462)
