package okta.example.reactive.webfluxtutorial

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import reactor.core.publisher.FluxSink
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Consumer


@Component
class ProfileCreatedEventPublisher(private val executor: Executor) :
    ApplicationListener<ProfileCreatedEvent>, Consumer<FluxSink<ProfileCreatedEvent>> {

    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    private val queue: BlockingQueue<ProfileCreatedEvent> = LinkedBlockingQueue()

    override fun onApplicationEvent(event: ProfileCreatedEvent) {
        log.info("Event received: [${event}]")
        queue.offer(event);
    }

    override fun accept(sink: FluxSink<ProfileCreatedEvent>) {
        executor.execute {
            while (true) try {
                val event = queue.take()
                sink.next(event)
            } catch (e: InterruptedException) {
                ReflectionUtils.rethrowRuntimeException(e)
            }
        }
    }

}
