import XCTest
import KontinuityCore
import KontinuityAsync

class AsyncFunctionTests: XCTestCase {
    
    private class TestValue { }

    func testCancellableInvoked() async {
        var cancelCount = 0
        let kontinuitySuspend: KontinuitySuspend<String, Error, Void> = { _, errorCallback in
            return {
                cancelCount += 1
                errorCallback(CancellationError(), ())
            }
        }
        let handle = Task {
            try await asyncFunction(for: kontinuitySuspend)
        }
        XCTAssertEqual(cancelCount, 0, "Cancellable shouldn't be invoked yet")
        handle.cancel()
        let result = await handle.result
        XCTAssertEqual(cancelCount, 1, "Cancellable should be invoked once")
        guard case .failure(_) = result else {
            XCTFail("Function should fail with an error")
            return
        }
    }
    
    func testCompletionWithValue() async {
        let value = TestValue()
        let kontinuitySuspend: KontinuitySuspend<TestValue, NSError, Void> = { resultCallback, _ in
            resultCallback(value, ())
            return { }
        }
        do {
            let receivedValue = try await asyncFunction(for: kontinuitySuspend)
            XCTAssertIdentical(receivedValue, value, "Received incorrect value")
        } catch {
            XCTFail("Function shouldn't throw an error")
        }
    }
    
    func testCompletionWithError() async {
        let sendError = NSError(domain: "Test", code: 0)
        let kontinuitySuspend: KontinuitySuspend<TestValue, NSError, Void> = { _, errorCallback in
            errorCallback(sendError, ())
            return { }
        }
        do {
            _ = try await asyncFunction(for: kontinuitySuspend)
            XCTFail("Function should throw an error")
        } catch {
            XCTAssertEqual(error as NSError, sendError, "Received incorrect error")
        }
    }
}
