public typealias KontinuityFlow<Output, Failure: Error, Unit> = (
    _ onItem: @escaping KontinuityCallback<Output, Unit>,
    _ onComplete: @escaping KontinuityCallback<Failure?, Unit>
) -> KontinuityCancellable<Unit>
