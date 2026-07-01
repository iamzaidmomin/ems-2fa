# OpenBao Integration Architecture

# Project
Employee Management System (EMS) – Two Factor Authentication Service

## Overview

This project integrates **OpenBao** with the FastAPI-based Two-Factor Authentication (2FA) service to securely manage SMTP credentials used for sending OTP emails.

Previously, SMTP credentials were stored in environment variables (`.env`). The new architecture introduces OpenBao as a centralized secret management solution while maintaining backward compatibility. If OpenBao is unavailable, the application automatically falls back to reading secrets from the `.env` file.

---

# Architecture

```
                    +----------------------+
                    |      User/API        |
                    +----------+-----------+
                               |
                               |
                               v
                    +----------------------+
                    |    FastAPI (2FA)     |
                    |   OTP Generation     |
                    +----------+-----------+
                               |
                 Request SMTP Configuration
                               |
                               v
                    +----------------------+
                    |   OpenBao Service    |
                    | (Secret Management)  |
                    +----------+-----------+
                               |
                      Read SMTP Secrets
                               |
                               v
                    +----------------------+
                    |     OpenBao KV       |
                    | SMTP Credentials     |
                    +----------------------+

If OpenBao is unavailable

FastAPI
    |
    +------> Load credentials from .env
```

---

# Components

## 1. FastAPI Application

Responsible for:

- OTP generation
- OTP validation
- Email sending
- Loading application configuration

---

## 2. OpenBao

OpenBao acts as the centralized secret manager.

Instead of hardcoding sensitive values inside configuration files, credentials are stored securely inside OpenBao's Key-Value (KV) Secret Engine.

Example secrets:

- SMTP_HOST
- SMTP_PORT
- SMTP_USER
- SMTP_PASSWORD
- SMTP_FROM

---

## 3. OpenBao Client (hvac)

The application communicates with OpenBao using the Python `hvac` library.

Responsibilities:

- Authenticate using OpenBao token
- Read SMTP credentials
- Return configuration to the application

---

## 4. Docker Compose

Docker Compose starts all services together.

Current services:

- OpenBao
- FastAPI TwoFA Service
- Employee Management Service

This creates an isolated local development environment.

---

# Secret Retrieval Flow

1. User requests an OTP.
2. FastAPI needs SMTP credentials.
3. OpenBaoService connects to OpenBao.
4. SMTP secrets are retrieved.
5. Email service sends the OTP.
6. If OpenBao cannot be reached:
   - Application loads SMTP values from `.env`.

This ensures high availability during development.

---

# Current Security Model

Current implementation:

- SMTP secrets stored in OpenBao
- Development token used
- Development mode enabled
- Automatic fallback to `.env`
- Secrets are no longer intended to be hardcoded inside source code

---

# Advantages

## Centralized Secret Management

All SMTP credentials are managed in one location.

---

## Better Security

Secrets are separated from application code.

---

## Easy Secret Rotation

Credentials can be updated without modifying application logic.

---

## Reduced Risk

Developers no longer need to commit SMTP credentials into Git repositories.

---

## Scalability

Multiple services can consume the same secrets from OpenBao.

---

# Current Limitations

Current implementation is intended for development.

Limitations include:

- Development mode
- Root token authentication
- Static token
- Local Docker deployment
- No authentication backend
- No TLS configuration

---

# Future Kubernetes Architecture

The long-term goal is to deploy this solution to Kubernetes.

```
                   Internet
                       |
                Kubernetes Ingress
                       |
              -----------------------
              |                     |
              v                     v
        FastAPI Pod          Employee Pod
              |                     |
              +----------+----------+
                         |
                         v
                  OpenBao Cluster
                         |
                         v
                  Persistent Storage
```

---

# Kubernetes Deployment Plan

## 1. Deploy OpenBao

Deploy OpenBao using:

- StatefulSet
- Persistent Volume
- Persistent Volume Claim

This ensures secrets survive pod restarts.

---

## 2. High Availability

Run multiple OpenBao replicas.

Benefits:

- Fault tolerance
- Improved availability
- Better scalability

---

## 3. Secure Authentication

Replace development token with:

- Kubernetes Service Accounts
- Kubernetes Authentication Method
- AppRole Authentication

---

## 4. TLS Encryption

Enable HTTPS communication between:

- FastAPI
- OpenBao

This protects secrets during transit.

---

## 5. Secret Rotation

Enable automatic secret rotation without restarting application containers.

---

## 6. RBAC

Configure Role-Based Access Control.

Each application receives access only to its required secrets.

---

## 7. Audit Logging

Enable OpenBao audit logs to monitor:

- Secret access
- Authentication events
- Unauthorized attempts

---

## 8. Monitoring

Integrate:

- Prometheus
- Grafana

Monitor:

- OpenBao health
- Secret requests
- Authentication failures
- Pod status

---

# Benefits of Kubernetes Migration

- Highly available secret management
- Centralized security
- Easier scaling
- Secure authentication
- Automatic recovery
- Better monitoring
- Enterprise-ready deployment
- Simplified secret rotation

---

# Future Improvements

- Replace development mode with production configuration.
- Enable TLS certificates.
- Implement Kubernetes authentication.
- Integrate CI/CD for automated deployments.
- Store database credentials in OpenBao.
- Store API keys and JWT secrets in OpenBao.
- Enable automated secret rotation.
- Configure backup and disaster recovery.
- Add monitoring and alerting.

---

# Conclusion

The current implementation introduces OpenBao as a secure secret management solution for SMTP credentials while preserving compatibility through `.env` fallback. Docker Compose provides a simple local development environment, and the design establishes a clear migration path toward a production-ready Kubernetes deployment with secure authentication, high availability, monitoring, and centralized secret management.