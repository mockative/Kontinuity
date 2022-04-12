import Combine

public typealias NativeCallback<T, Unit> = (T, Unit) -> Unit

public typealias NativeCancellable<Unit> = () -> Unit

public typealias NativeSuspend<Result, Failure: Error, Unit> = (
    _ onResult: @escaping NativeCallback<Result, Unit>,
    _ onError: @escaping NativeCallback<Failure, Unit>
) -> NativeCancellable<Unit>

public typealias NativeFlow<Output, Failure: Error, Unit> = (
    _ onItem: @escaping NativeCallback<Output, Unit>,
    _ onComplete: @escaping NativeCallback<Failure?, Unit>
) -> NativeCancellable<Unit>

public typealias NativeStateFlow<Output, Failure: Error, Unit> = (
    _ mode: String,
    _ setValue: @escaping NativeCallback<Output, Unit>,
    _ onItem: @escaping NativeCallback<Output, Unit>,
    _ onComplete: @escaping NativeCallback<Failure?, Unit>
) -> NativeCancellable<Unit>

// MARK: Flow
public func createPublisher<Output, Failure: Error, Unit>(
    for nativeFlow: @escaping NativeFlow<Output, Failure, Unit>
) -> AnyPublisher<Output, Failure> {
    NativeFlowPublisher(nativeFlow: nativeFlow)
        .eraseToAnyPublisher()
}

internal struct NativeFlowPublisher<Output, Failure: Error, Unit>: Publisher {
    let nativeFlow: NativeFlow<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = NativeFlowSubscription(nativeFlow: nativeFlow, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class NativeFlowSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var nativeCancellable: NativeCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(nativeFlow: NativeFlow<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.nativeCancellable = nativeFlow(
            { item, unit in
                _ = self.subscriber?.receive(item)
                return unit
            },
            { error, unit in
                if let error = error {
                    self.subscriber?.receive(completion: .failure(error))
                } else {
                    self.subscriber?.receive(completion: .finished)
                }
                
                return unit
            }
        )
    }
    
    func request(_ demand: Subscribers.Demand) {
        // Nothing
    }
    
    func cancel() {
        subscriber = nil
        
        _ = nativeCancellable?()
        nativeCancellable = nil
    }
}

// MARK: NativeStateFlow
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

internal struct NativeStateFlowPublisher<Output, Failure: Error, Unit>: Publisher {
    let nativeStateFlow: NativeStateFlow<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = NativeStateFlowSubscription(nativeStateFlow: nativeStateFlow, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class NativeStateFlowSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var nativeCancellable: NativeCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(nativeStateFlow: NativeStateFlow<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.nativeCancellable = nativeStateFlow(
            "subscribe",
            { value, unit in
                // This won't actually be called
                return unit
            },
            { item, unit in
                _ = self.subscriber?.receive(item)
                return unit
            },
            { error, unit in
                if let error = error {
                    self.subscriber?.receive(completion: .failure(error))
                } else {
                    self.subscriber?.receive(completion: .finished)
                }
                
                return unit
            }
        )
    }
    
    func request(_ demand: Subscribers.Demand) {
        // Nothing
    }
    
    func cancel() {
        subscriber = nil
        
        _ = nativeCancellable?()
        nativeCancellable = nil
    }
}
