package org.axonframework.metrics;

import com.codahale.metrics.*;
import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Times allTimer messages, successful and failed messages
 *
 * @author Marijn van Zelst
 * @since 3.0
 */
public class MessageTimerMonitor implements MessageMonitor<Message<?>>, MetricSet {

    private final Timer allTimer;
    private final Timer successTimer;
    private final Timer failureTimer;
    private final Timer ignoredTimer;

    /**
     * Creates a MessageTimerMonitor using a default clock
     */
    public MessageTimerMonitor() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a MessageTimerMonitor using the provided clock
     *
     * @param clock the clock used to measure the process time of each message
     */
    public MessageTimerMonitor(Clock clock) {
        allTimer = new Timer(new ExponentiallyDecayingReservoir(), clock);
        successTimer = new Timer(new ExponentiallyDecayingReservoir(), clock);
        failureTimer = new Timer(new ExponentiallyDecayingReservoir(), clock);
        ignoredTimer = new Timer(new ExponentiallyDecayingReservoir(), clock);
    }

    @Override
    public MonitorCallback onMessageIngested(Message<?> message) {
        final Timer.Context allTimerContext = this.allTimer.time();
        final Timer.Context successTimerContext = this.successTimer.time();
        final Timer.Context failureTimerContext = this.failureTimer.time();
        final Timer.Context ignoredTimerContext = this.ignoredTimer.time();
        return new MessageMonitor.MonitorCallback() {
            @Override
            public void reportSuccess() {
                allTimerContext.stop();
                successTimerContext.stop();
            }

            @Override
            public void reportFailure(Throwable cause) {
                allTimerContext.stop();
                failureTimerContext.stop();
            }

            @Override
            public void reportIgnored() {
                allTimerContext.stop();
                ignoredTimerContext.stop();
            }
        };
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("allTimer", allTimer);
        metrics.put("successTimer", successTimer);
        metrics.put("failureTimer", failureTimer);
        metrics.put("ignoredTimer", ignoredTimer);
        return metrics;
    }
}