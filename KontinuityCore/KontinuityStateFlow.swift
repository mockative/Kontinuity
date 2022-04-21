public typealias KontinuityStateFlow<Output, Failure: Error, Unit> = (
    _ mode: String,
    _ setValue: @escaping KontinuityCallback<Output, Unit>,
    _ onItem: @escaping KontinuityCallback<Output, Unit>,
    _ onComplete: @escaping KontinuityCallback<Failure?, Unit>
) -> KontinuityCancellable<Unit>

public func getValue<Output, Failure: Error, Unit>(
    of kontinuityStateFlow: @escaping KontinuityStateFlow<Output, Failure, Unit>
) -> Output {
    var receivedValue: Output? = nil
    
    _ = kontinuityStateFlow(
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
