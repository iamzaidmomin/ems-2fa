import importlib
import unittest
from unittest.mock import patch

import app.config as config
import app.services.openbao as openbao


class OpenBaoConfigTests(unittest.TestCase):
    def test_get_settings_prefers_openbao_smtp_values(self):
        class FakeOpenBaoService:
            def get_smtp_config(self):
                return {
                    "SMTP_HOST": "smtp.openbao.test",
                    "SMTP_PORT": 2525,
                    "SMTP_USER": "openbao-user",
                    "SMTP_PASSWORD": "openbao-password",
                    "SMTP_FROM": "mailer@openbao.test",
                }

        with patch.object(openbao, "OpenBaoService", FakeOpenBaoService):
            config = importlib.reload(config)
            config.get_settings.cache_clear()
            settings = config.get_settings()

        self.assertEqual(settings.SMTP_HOST, "smtp.openbao.test")
        self.assertEqual(settings.SMTP_PORT, 2525)
        self.assertEqual(settings.SMTP_USER, "openbao-user")
        self.assertEqual(settings.SMTP_PASSWORD, "openbao-password")
        self.assertEqual(settings.SMTP_FROM, "mailer@openbao.test")


if __name__ == "__main__":
    unittest.main()
