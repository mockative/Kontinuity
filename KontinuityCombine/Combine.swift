import Combine
import KontinuityCore

// MARK: Suspend
public func createFuture<Result, Failure: Error, Unit>(
    for kontinuitySuspend: @escaping KontinuitySuspend<Result, Failure, Unit>
) -> AnyPublisher<Result, Failure> {
    KontinuitySuspendPublisher(kontinuitySuspend: kontinuitySuspend)
        .eraseToAnyPublisher()
}

// MARK: Flow
public func createPublisher<Output, Failure: Error, Unit>(
    for kontinuityFlow: @escaping KontinuityFlow<Output, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    KontinuityFlowPublisher(kontinuityFlow: kontinuityFlow)
        .eraseToAnyPublisher()
}

// MARK: StateFlow
public func createPublisher<Output, Failure: Error, Unit>(
    for kontinuityStateFlow: @escaping KontinuityStateFlow<Output, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    KontinuityStateFlowPublisher(kontinuityStateFlow: kontinuityStateFlow)
        .eraseToAnyPublisher()
}

// MARK: Suspend + Flow
public func createPublisher<Output, Failure: Error, Unit>(
    for kontinuitySuspend: @escaping KontinuitySuspend<KontinuityFlow<Output, Failure, Unit>, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    return createFuture(for: kontinuitySuspend)
        .flatMap { kontinuityFlow in createPublisher(for: kontinuityFlow) }
        .eraseToAnyPublisher()
}

// MARK: Suspend + StateFlow
public func createPublisher<Output, Failure: Error, Unit>(
    for kontinuitySuspend: @escaping KontinuitySuspend<KontinuityStateFlow<Output, Failure, Unit>, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    return createFuture(for: kontinuitySuspend)
        .flatMap { kontinuityFlow in createPublisher(for: kontinuityFlow) }
        .eraseToAnyPublisher()
}
