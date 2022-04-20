public typealias KontinuityStateFlow<Output, Failure: Error, Unit> = (
    _ mode: String,
    _ setValue: @escaping KontinuityCallback<Output, Unit>,
    _ onItem: @escaping KontinuityCallback<Output, Unit>,
    _ onComplete: @escaping KontinuityCallback<Failure?, Unit>
) -> KontinuityCancellable<Unit>
