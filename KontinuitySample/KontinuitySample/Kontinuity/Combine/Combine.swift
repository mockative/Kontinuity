import Combine

// MARK: Suspend
public func createFuture<Result, Failure: Error, Unit>(
    for nativeSuspend: @escaping NativeSuspend<Result, Failure, Unit>
) -> AnyPublisher<Result, Failure> {
    NativeSuspendPublisher(nativeSuspend: nativeSuspend)
        .eraseToAnyPublisher()
}

// MARK: Flow
public func createPublisher<Output, Failure: Error, Unit>(
    for nativeFlow: @escaping NativeFlow<Output, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    NativeFlowPublisher(nativeFlow: nativeFlow)
        .eraseToAnyPublisher()
}

// MARK: StateFlow
public func createPublisher<Output, Failure: Error, Unit>(
    for nativeStateFlow: @escaping NativeStateFlow<Output, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    NativeStateFlowPublisher(nativeStateFlow: nativeStateFlow)
        .eraseToAnyPublisher()
}

public func getValue<Output, Failure: Error, Unit>(
    of nativeStateFlow: @escaping NativeStateFlow<Output, Failure, Unit>
) -> Output {
    var receivedValue: Output? = nil
    
    _ = nativeStateFlow(
        "receive",
        { value, unit in
            // This is called synchronously
            receivedValue = value
            return unit
        },
        { _, unit in
            // This won't actually be called
            return unit
        },
        { _, unit in
            // This won't actually be called
            return unit
        }
    )
    
    return receivedValue!
}

// MARK: Suspend + Flow
public func createPublisher<Output, Failure: Error, Unit>(
    for nativeSuspend: @escaping NativeSuspend<NativeFlow<Output, Failure, Unit>, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    return createFuture(for: nativeSuspend)
        .flatMap { nativeFlow in createPublisher(for: nativeFlow) }
        .eraseToAnyPublisher()
}
