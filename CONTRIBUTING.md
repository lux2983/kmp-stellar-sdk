# Contributing

We welcome contributions to the Stellar SDK for Kotlin Multiplatform. Whether you're fixing bugs, adding features, or improving documentation, your help is appreciated.

## How to Contribute

### Reporting Issues
- Check existing issues before creating a new one
- Provide clear reproduction steps and environment details
- Include relevant code samples and error messages

### Proposing Features
- Open an issue to discuss the feature before implementation
- Explain the use case and expected behavior
- Consider cross-platform implications

### Contributing Code
1. Fork the repository
2. Create a feature branch (`feature/your-feature-name`)
3. Implement your changes with tests
4. Ensure JVM tests pass
5. Submit a pull request

## Development Guidelines

### Setup
See [CLAUDE.md](CLAUDE.md) for detailed development guidelines and architecture.

### Testing
- Add tests for all new functionality
- Ensure JVM tests pass: `./gradlew :stellar-sdk:jvmTest`
- Platform-specific changes should be tested on the target platform when possible

### Code Quality
- Follow existing code style and conventions
- Write clear, descriptive commit messages
- Keep commits focused and atomic
- Update documentation as needed

### Pull Request Checklist
- [ ] Tests added and passing on JVM
- [ ] Documentation updated
- [ ] Code follows project conventions
- [ ] Commit messages are clear
- [ ] No breaking changes (or clearly documented)

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
