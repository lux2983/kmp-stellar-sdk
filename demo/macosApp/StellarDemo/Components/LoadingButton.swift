import SwiftUI

/// Reusable loading button component with consistent styling across the app.
///
/// This component provides:
/// - Loading state with spinner and text
/// - Disabled state visual feedback
/// - Icon + text layout
/// - Two style variants: filled and outlined
/// - Consistent dimensions and corner radius
///
/// Example usage:
/// ```swift
/// LoadingButton(
///     action: { performAction() },
///     isLoading: isLoading,
///     isEnabled: isFormValid && !isLoading,
///     icon: "arrow.clockwise",
///     text: "Generate Keypair",
///     loadingText: "Generating...",
///     style: .filled
/// )
/// ```
struct LoadingButton: View {
    let action: () -> Void
    let isLoading: Bool
    let isEnabled: Bool
    let icon: String
    let text: String
    let loadingText: String
    let style: ButtonStyle

    // Optional customization
    var backgroundColor: Color?
    var iconSize: CGFloat = 16
    var textSize: CGFloat = 17
    var height: CGFloat = 56

    enum ButtonStyle {
        case filled
        case outlined
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: style == .filled ? 8 : 4) {
                if isLoading {
                    ProgressView()
                        .controlSize(.small)
                        .tint(style == .filled ? .white : nil)
                    Text(loadingText)
                        .font(.system(size: style == .filled ? textSize : textSize - 4))
                } else {
                    Image(systemName: icon)
                        .font(.system(size: iconSize))
                    Text(text)
                        .font(.system(size: style == .filled ? textSize : textSize - 4))
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: height)
            .foregroundColor(foregroundColor)
            .background(background)
            .cornerRadius(12)
            .overlay(overlay)
        }
        .disabled(!isEnabled)
        .buttonStyle(.plain)
    }

    // MARK: - Style Computation

    private var foregroundColor: Color {
        switch style {
        case .filled:
            return .white
        case .outlined:
            return Material3Colors.primary
        }
    }

    private var background: Color {
        switch style {
        case .filled:
            let baseColor = backgroundColor ?? Material3Colors.primary
            return isEnabled ? baseColor : baseColor.opacity(0.6)
        case .outlined:
            return .clear
        }
    }

    @ViewBuilder
    private var overlay: some View {
        if style == .outlined {
            RoundedRectangle(cornerRadius: 12)
                .stroke(Material3Colors.primary, lineWidth: 1)
        }
    }
}

// MARK: - Convenience Initializers

extension LoadingButton {
    /// Create a filled LoadingButton with default styling
    init(
        action: @escaping () -> Void,
        isLoading: Bool,
        isEnabled: Bool,
        icon: String,
        text: String,
        loadingText: String,
        backgroundColor: Color? = nil
    ) {
        self.init(
            action: action,
            isLoading: isLoading,
            isEnabled: isEnabled,
            icon: icon,
            text: text,
            loadingText: loadingText,
            style: .filled,
            backgroundColor: backgroundColor
        )
    }
}

// MARK: - Preview

#Preview("Filled Enabled") {
    VStack(spacing: 16) {
        LoadingButton(
            action: {},
            isLoading: false,
            isEnabled: true,
            icon: "arrow.clockwise",
            text: "Generate Keypair",
            loadingText: "Generating...",
            style: .filled
        )
        .padding()
    }
}

#Preview("Filled Loading") {
    VStack(spacing: 16) {
        LoadingButton(
            action: {},
            isLoading: true,
            isEnabled: false,
            icon: "arrow.clockwise",
            text: "Generate Keypair",
            loadingText: "Generating...",
            style: .filled
        )
        .padding()
    }
}

#Preview("Filled Disabled") {
    VStack(spacing: 16) {
        LoadingButton(
            action: {},
            isLoading: false,
            isEnabled: false,
            icon: "arrow.clockwise",
            text: "Generate Keypair",
            loadingText: "Generating...",
            style: .filled
        )
        .padding()
    }
}

#Preview("Outlined Enabled") {
    VStack(spacing: 16) {
        LoadingButton(
            action: {},
            isLoading: false,
            isEnabled: true,
            icon: "arrow.clockwise",
            text: "Generate & Fill",
            loadingText: "Generating...",
            style: .outlined
        )
        .padding()
    }
}

#Preview("Outlined Loading") {
    VStack(spacing: 16) {
        LoadingButton(
            action: {},
            isLoading: true,
            isEnabled: false,
            icon: "arrow.clockwise",
            text: "Generate & Fill",
            loadingText: "Generating...",
            style: .outlined
        )
        .padding()
    }
}

#Preview("Custom Color") {
    VStack(spacing: 16) {
        LoadingButton(
            action: {},
            isLoading: false,
            isEnabled: true,
            icon: "play.circle.fill",
            text: "Simulate Read Call",
            loadingText: "Simulating...",
            style: .filled,
            backgroundColor: Material3Colors.onSuccessContainer
        )
        .padding()
    }
}
