import SwiftUI
import shared

struct AccountDetailsScreen: View {
    @ObservedObject var toastManager: ToastManager
    @State private var accountId = ""
    @State private var isFetching = false
    @State private var detailsResult: AccountDetailsResult?
    @State private var validationError: String?

    @EnvironmentObject var bridgeWrapper: MacOSBridgeWrapper

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 16) {
                    infoCard
                    inputField
                    fetchButton
                    resultView
                    placeholderView
                }
                .padding(16)
            }
            .background(Material3Colors.surface)
            .navigationToolbar(title: "Fetch Account Details")
            .onChange(of: detailsResult) { newValue in
                // Smart auto-scroll: reveal result without scrolling too far
                if newValue != nil {
                    withAnimation {
                        proxy.scrollTo("resultCard", anchor: .top)
                    }
                }
            }
        }
    }

    // MARK: - View Components

    private var infoCard: some View {
        InfoCard(title: "Horizon API: fetch account details", color: .secondary) {
            Text("Enter a Stellar account ID to retrieve comprehensive account information including balances, signers, thresholds, and more.")
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSecondaryContainer)
        }
    }

    private var inputField: some View {
        StellarTextField(
            label: "Account ID",
            placeholder: "G...",
            text: $accountId,
            error: validationError,
            useMonospacedFont: true,
            backgroundColor: .white
        )
        .onChange(of: accountId) { _ in
            validationError = nil
            detailsResult = nil
        }
    }

    private var fetchButton: some View {
        LoadingButton(
            action: fetchDetails,
            isLoading: isFetching,
            isEnabled: !isFetching && !accountId.isEmpty,
            icon: "magnifyingglass",
            text: "Fetch Details",
            loadingText: "Fetching..."
        )
    }

    @ViewBuilder
    private var resultView: some View {
        if let result = detailsResult {
            switch result {
            case let success as AccountDetailsResult.Success:
                AccountDetailsCard(account: success.accountResponse)
                    .id("resultCard")
            case let error as AccountDetailsResult.Error:
                errorCard(error)
                    .id("resultCard")
                troubleshootingCard
            default:
                EmptyView()
            }
        }
    }

    private func errorCard(_ error: AccountDetailsResult.Error) -> some View {
        InfoCard(title: "Error", color: .error) {
            Text(error.message)
                .font(.system(size: 14))
                .foregroundStyle(Material3Colors.onErrorContainer)

            if let exception = error.exception {
                Text("Technical details: \(exception.message ?? "Unknown error")")
                    .font(.system(size: 13, design: .monospaced))
                    .foregroundStyle(Material3Colors.onErrorContainer)
                    .padding(.top, 4)
            }
        }
    }

    private var troubleshootingCard: some View {
        InfoCard(title: "Troubleshooting", color: .secondary) {
            VStack(alignment: .leading, spacing: 4) {
                Text("• Verify the account ID is valid (starts with 'G' and is 56 characters)")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Make sure the account exists on testnet (fund it via Friendbot if needed)")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Check your internet connection")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Try again in a moment if you're being rate-limited")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)
            }
        }
    }

    @ViewBuilder
    private var placeholderView: some View {
        if detailsResult == nil && !isFetching && accountId.isEmpty {
            Spacer()
                .frame(height: 16)

            Image(systemName: "person.text.rectangle")
                .font(.system(size: 64))
                .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.3))

            Text("Enter an account ID to view its details on the testnet")
                .font(.system(size: 14))
                .foregroundStyle(Material3Colors.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
    }

    // MARK: - Actions

    private func fetchDetails() {
        // Validate before fetching
        if let error = FormValidation.validateAccountIdField(accountId) {
            validationError = error
            toastManager.show(error)
            return
        }

        isFetching = true
        detailsResult = nil

        Task {
            do {
                let result = try await bridgeWrapper.bridge.fetchAccountDetails(accountId: accountId)
                await MainActor.run {
                    detailsResult = result
                    isFetching = false
                }
            } catch {
                await MainActor.run {
                    detailsResult = AccountDetailsResult.Error(
                        message: "Failed to fetch account details: \(error.localizedDescription)",
                        exception: error as? KotlinThrowable
                    )
                    isFetching = false
                }
            }
        }
    }

    private func shortenAccountId(_ id: String) -> String {
        if id.count > 12 {
            return "\(id.prefix(4))...\(id.suffix(4))"
        }
        return id
    }
}

// MARK: - Trust Asset Screen (matches Compose TrustAssetScreen)

