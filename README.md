# V-Routing-Protocol

An implementation of an ad-hoc routing protocol that operates on top of a network of series of sockets. This project focuses on providing a node-centric implementation while supporting comprehensive testing scenarios for multiple node configurations.

## Project Structure

```
src/
└── main/
    └── java/
        └── com/
            └── vrouting/
                └── network/
                    └── socket/
                        ├── ConnectionMetrics.java
                        ├── NetworkSocket.java
                        ├── TCPNetworkSocket.java
                        └── SocketPool.java
```

## Features

- Node-centric implementation of ad-hoc networking
- Dynamic Source Routing (DSR) implementation
- Connection pooling and management
- Health monitoring and metrics tracking
- Automatic connection cleanup
- Thread-safe implementation

## Development Plan

The project is being developed in phases as outlined in `DevelopmentPlan.md`. Current implementation includes:

### Phase 1: Core Network Infrastructure
- Base socket implementation
- Connection metrics tracking
- Socket pooling
- Health monitoring

## Getting Started

### Prerequisites
- Java 8 or higher
- Git

### Building the Project
```bash
# Clone the repository
git clone https://github.com/salehelsayed/V-Routing-Protocol.git

# Navigate to project directory
cd V-Routing-Protocol

# Build the project (build system to be added)
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
