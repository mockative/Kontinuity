import KontinuityCore

/// Awaits the `KontinuitySuspend` and returns the result.
/// - Parameter kontinuitySuspend: The kontinuity suspend function to await.
/// - Returns: The `Result` from the `kontinuitySuspend`.
public func asyncResult<Output, Failure: Error, Unit>(for kontinuitySuspend: @escaping KontinuitySuspend<Output, Failure, Unit>) async -> Result<Output, Error> {
    do {
        return .success(try await asyncFunction(for: kontinuitySuspend))
    } catch {
        return .failure(error)
    }
}
