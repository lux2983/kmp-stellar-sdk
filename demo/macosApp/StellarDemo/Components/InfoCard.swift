import SwiftUI

struct InfoCard<Content: View>: View {
    let title: String?
    let color: CardColor
    @ViewBuilder let content: () -> Content

    enum CardColor {
        case `default`
        case primary
        case secondary
        case tertiary
        case success
        case error
        case warning
        case surfaceVariant

        var backgroundColor: Color {
            switch self {
            case .default:
                return Material3Colors.surface
            case .primary:
                return Material3Colors.primaryContainer
            case .secondary:
                return Material3Colors.secondaryContainer
            case .tertiary:
                return Material3Colors.tertiaryContainer
            case .success:
                return Material3Colors.successContainer
            case .error:
                return Material3Colors.errorContainer
            case .warning:
                return Material3Colors.tertiaryContainer
            case .surfaceVariant:
                return Material3Colors.surfaceVariant
            }
        }

        var textColor: Color {
            switch self {
            case .default:
                return Material3Colors.onSurface
            case .primary:
                return Material3Colors.onPrimaryContainer
            case .secondary:
                return Material3Colors.onSecondaryContainer
            case .tertiary:
                return Material3Colors.onTertiaryContainer
            case .success:
                return Material3Colors.onSuccessContainer
            case .error:
                return Material3Colors.onErrorContainer
            case .warning:
                return Material3Colors.onTertiaryContainer
            case .surfaceVariant:
                return Material3Colors.onSurfaceVariant
            }
        }

        var opacity: Double {
            return 1.0
        }
    }

    init(
        title: String? = nil,
        color: CardColor = .default,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.title = title
        self.color = color
        self.content = content
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let title = title {
                Text(title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(color.textColor)
            }

            content()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(color.backgroundColor.opacity(color.opacity))
        .cornerRadius(12)
        .shadow(color: Material3Colors.cardShadow, radius: 2, x: 0, y: 1)
    }
}

extension InfoCard.CardColor {
    init(transparentPrimary: Bool) {
        self = .primary
    }

    init(transparentError: Bool) {
        self = .error
    }

    var withOpacity: InfoCard.CardColor {
        switch self {
        case .primary:
            return .primary
        case .error:
            return .error
        case .tertiary:
            return .tertiary
        default:
            return self
        }
    }
}

struct TransparentInfoCard<Content: View>: View {
    let title: String?
    let backgroundColor: Color
    @ViewBuilder let content: () -> Content

    init(
        title: String? = nil,
        backgroundColor: Color,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.title = title
        self.backgroundColor = backgroundColor
        self.content = content
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let title = title {
                Text(title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(Material3Colors.onSurface)
            }

            content()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(backgroundColor)
        .cornerRadius(12)
        .shadow(color: Material3Colors.cardShadow, radius: 2, x: 0, y: 1)
    }
}
