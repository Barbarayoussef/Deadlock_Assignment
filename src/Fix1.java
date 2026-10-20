import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Fix1 {
    private static class Account {
        final String name;
        int balance = 100;
        final ReentrantLock lock = new ReentrantLock();
        Account(String name) { this.name = name; }
    }

    private static void transfer(Account from,Account to, int amount) {
        Account firstLock = from;
        Account secondLock = to;
        if(firstLock.name.compareTo(secondLock.name) >= 0) {
            firstLock=to;
            secondLock=from;
        }
        firstLock.lock.lock();
        System.out.println("Grabbed " + from.name + " lock");
        try {
            secondLock.lock.lock();
            System.out.println("Grabbed " + to.name + " lock");
            try {
                TimeUnit.SECONDS.sleep(2);

                from.balance -= amount;
                to.balance += amount;
                System.out.println("Transfer amount " + amount
                        + " from " + from.name + "->" + to.name + " complete");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                secondLock.lock.unlock();
                System.out.println("Released " + secondLock.name + " lock");
            }
        }finally{
            firstLock.lock.unlock();
            System.out.println("Released " + firstLock.name + " lock");
        }



    }
    public static void main(String[] args) throws InterruptedException {
        Account a = new Account("A");
        Account b = new Account("B");
        Thread t1 = new Thread(() -> transfer(a, b, 10));
        Thread t2 = new Thread(() -> transfer(b, a, 3));
        t1.start();
        t2.start();
        TimeUnit.SECONDS.sleep(2);
        System.out.println("Done. A = " + a.balance + " B = " + b.balance);
    }
}
