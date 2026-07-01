import os
import hvac


class OpenBaoService:
    def __init__(self):
        self.client = hvac.Client(
            url=os.getenv("OPENBAO_ADDR", "http://openbao:8200"),
            token=os.getenv("OPENBAO_TOKEN", "root"),
        )

    def get_smtp_config(self):
        try:
            secret = self.client.secrets.kv.v2.read_secret_version(
                path="smtp"
            )

            return secret["data"]["data"]

        except Exception:
            # Fallback values if OpenBao is unavailable
            return {
                "SMTP_HOST": "smtp.gmail.com",
                "SMTP_PORT": 587,
                "SMTP_USER": "demo@example.com",
                "SMTP_PASSWORD": "retrieved-from-openbao",
                "SMTP_FROM": "demo@example.com",
            }