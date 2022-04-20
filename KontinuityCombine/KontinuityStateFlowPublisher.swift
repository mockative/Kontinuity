import Combine
import KontinuityCore

internal struct KontinuityStateFlowPublisher<Output, Failure: Error, Unit>: Publisher {
    let kontinuityStateFlow: KontinuityStateFlow<Output, Failure, Unit>
    
    func receive<S>(subscriber: S) where S : Subscriber, Failure == S.Failure, Output == S.Input {
        let subscription = KontinuityStateFlowSubscription(kontinuityStateFlow: kontinuityStateFlow, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

internal class KontinuityStateFlowSubscription<Output, Failure, Unit, S: Subscriber>: Subscription where S.Input == Output, S.Failure == Failure {
    private var kontinuityCancellable: KontinuityCancellable<Unit>? = nil
    private var subscriber: S?
    
    init(kontinuityStateFlow: KontinuityStateFlow<Output, Failure, Unit>, subscriber: S) {
        self.subscriber = subscriber
        self.kontinuityCancellable = kontinuityStateFlow(
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
        
        _ = kontinuityCancellable?()
        kontinuityCancellable = nil
    }
}
