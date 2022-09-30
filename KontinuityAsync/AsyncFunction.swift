import KontinuityCore

/// Wraps the `KontinuitySuspend` in an async function.
/// - Parameter kontinuitySuspend: The Kontinuity suspend function to await.
/// - Returns: The result from the `kontinuitySuspend`.
/// - Throws: Errors thrown by the `kontinuitySuspend`.
public func asyncFunction<Result, Failure: Error, Unit>(for kontinuitySuspend: @escaping KontinuitySuspend<Result, Failure, Unit>) async throws -> Result {
    let asyncFunctionActor = AsyncFunctionActor<Result, Unit>()
    return try await withTaskCancellationHandler {
        Task { await asyncFunctionActor.cancel() }
    } operation: {
        try await withUnsafeThrowingContinuation { continuation in
            Task {
                await asyncFunctionActor.setContinuation(continuation)
                let kontinuityCancellable = kontinuitySuspend({ output, unit in
                    Task { await asyncFunctionActor.continueWith(result: output) }
                    return unit
                }, { error, unit in
                    Task { await asyncFunctionActor.continueWith(error: error) }
                    return unit
                })
                await asyncFunctionActor.setKontinuityCancellable(kontinuityCancellable)
            }
        }
    }
}

internal actor AsyncFunctionActor<Result, Unit> {
    
    private var isCancelled = false
    private var kontinuityCancellable: KontinuityCancellable<Unit>? = nil
    
    func setKontinuityCancellable(_ kontinuityCancellable: @escaping KontinuityCancellable<Unit>) {
        guard !isCancelled else {
            _ = kontinuityCancellable()
            return
        }
        self.kontinuityCancellable = kontinuityCancellable
    }
    
    func cancel() {
        isCancelled = true
        _ = kontinuityCancellable?()
        kontinuityCancellable = nil
    }
    
    private var continuation: UnsafeContinuation<Result, Error>? = nil
    
    func setContinuation(_ continuation: UnsafeContinuation<Result, Error>) {
        self.continuation = continuation
    }
    
    func continueWith(result: Result) {
        continuation?.resume(returning: result)
        continuation = nil
    }
    
    func continueWith(error: Error) {
        continuation?.resume(throwing: error)
        continuation = nil
    }
}
