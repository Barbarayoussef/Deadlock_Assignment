import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Fix2 {
    static class Account {
        final String name;
        int balance = 100;
        final ReentrantLock lock = new ReentrantLock();
        Account(String name) { this.name = name; }
    }

    private static void transfer(Account from, Account to, int amount) {
        Random random = new Random();
        boolean acquiredFrom = false;
        boolean acquiredTo = false;

        while (!acquiredTo) {
            try {
                acquiredFrom = from.lock.tryLock();
                if (!acquiredFrom) {
                    Thread.sleep(random.nextInt(50) + 10);
                    continue;
                }

                acquiredTo = to.lock.tryLock(100, TimeUnit.MILLISECONDS);

                if (!acquiredTo) {
                    from.lock.unlock();
                    acquiredFrom = false;

                    Thread.sleep(random.nextInt(50) + 10);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Transfer interrupted for " + from.name + " -> " + to.name);
                return;
            }
        }

        try {
            System.out.println("Grabbed both " + from.name + " and " + to.name + " locks");

            TimeUnit.SECONDS.sleep(1);

            from.balance -= amount;
            to.balance += amount;
            System.out.println("Transfer amount " + amount
                    + " from " + from.name + " -> " + to.name + " complete");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (acquiredTo) {
                to.lock.unlock();
            }
            if (acquiredFrom) {
                from.lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Account a = new Account("A");
        Account b = new Account("B");

        Thread t1 = new Thread(() -> transfer(a, b, 10));
        Thread t2 = new Thread(() -> transfer(b, a, 3));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Done. A = " + a.balance + " B = " + b.balance);
    }
}