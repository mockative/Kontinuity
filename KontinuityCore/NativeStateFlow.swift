public typealias NativeStateFlow<Output, Failure: Error, Unit> = (
    _ mode: String,
    _ setValue: @escaping NativeCallback<Output, Unit>,
    _ onItem: @escaping NativeCallback<Output, Unit>,
    _ onComplete: @escaping NativeCallback<Failure?, Unit>
) -> NativeCancellable<Unit>
