package com.jero.snowflake;

import cn.hutool.core.util.StrUtil;
import com.jero.snowflake.impl.DefaultUidGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link DefaultUidGenerator}
 *
 * @author yutianbao
 */
public class DefaultUidGeneratorTest {
    private static final int SIZE = 7000; // 700w
    private static final boolean VERBOSE = true;
    private static final int THREADS = Runtime.getRuntime().availableProcessors() << 1;

    private UidGenerator uidGenerator = DefaultUidGenerator.getUidGenerator();

    /**
     * Test for serially generate
     */
    @Test
    public void testSerialGenerate() {
        // Generate UID serially
        Set<Long> uidSet = new HashSet<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            doGenerate(uidSet, i);
        }

        // Check UIDs are all unique
        checkUniqueID(uidSet);
    }

    /**
     * Test for parallel generate
     *
     * @throws InterruptedException
     */
    @Test
    public void testParallelGenerate() throws InterruptedException {
        AtomicInteger control = new AtomicInteger(-1);
        Set<Long> uidSet = new ConcurrentSkipListSet<>();

        // Initialize threads
        List<Thread> threadList = new ArrayList<>(THREADS);
        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(() -> workerRun(uidSet, control));
            thread.setName("UID-generator-" + i);

            threadList.add(thread);
            thread.start();
        }

        // Wait for worker done
        for (Thread thread : threadList) {
            thread.join();
        }

        // Check generate 10w times
        assertEquals(SIZE, control.get());

        // Check UIDs are all unique
        checkUniqueID(uidSet);
    }

    /**
     * Worker run
     */
    private void workerRun(Set<Long> uidSet, AtomicInteger control) {
        for (; ; ) {
            int myPosition = control.updateAndGet(old -> (old == SIZE ? SIZE : old + 1));
            if (myPosition == SIZE) {
                return;
            }

            doGenerate(uidSet, myPosition);
        }
    }

    /**
     * Do generating
     */
    private void doGenerate(Set<Long> uidSet, int index) {
        long uid = uidGenerator.getUID();
        String parsedInfo = uidGenerator.parseUID(uid);
        uidSet.add(uid);

        // Check UID is positive, and can be parsed
        assertTrue(uid > 0L);
        assertTrue(StrUtil.isNotBlank(parsedInfo));

        if (VERBOSE) {
            System.out.println(Thread.currentThread().getName() + " No." + index + " >>> " + parsedInfo);
        }
    }

    /**
     * Check UIDs are all unique
     */
    private void checkUniqueID(Set<Long> uidSet) {
        assertEquals(SIZE, uidSet.size());
    }

}
