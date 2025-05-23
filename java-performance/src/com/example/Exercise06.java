package com.example;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Exercise06 {
    private static final int BUFFER_SIZE = 1024 * 1024; // 1 MB buffer
    private static final String FILE_NAME = "io_wait_with_lock.dat";
    private static final long FILE_SIZE = 1024 * 1024 * 1024; // 1 GB file size
    private static final int THREAD_COUNT = 4; // Number of concurrent threads

    private static final ReentrantLock lock = new ReentrantLock(); // Shared lock

    public static void main(String[] args) {
        var executorService = newFixedThreadPool(THREAD_COUNT);

        for (var i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    performLockedIO();
                } catch (IOException e) {
                    System.err.println("Error during I/O: " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
    }

    private static void performLockedIO() throws IOException {
        var file = new File(FILE_NAME);
        var buffer = new byte[BUFFER_SIZE];
        new Random().nextBytes(buffer);

        while (true) {
            // Acquire lock before performing I/O
            lock.lock();
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                var bytesWritten = 0;
                while (bytesWritten < FILE_SIZE) {
                    fos.write(buffer);
                    bytesWritten += buffer.length;
                }
                fos.getFD().sync(); // Force flush to disk
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            } finally {
                lock.unlock();
            }
        }
    }
}