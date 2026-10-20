import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static class Account {
        final String name;
        int balance = 100;
        final ReentrantLock lock = new ReentrantLock();
        Account(String name) { this.name = name; }
    }

    private static void transfer(Account from, Account to, int amount) {
        from.lock.lock();
        System.out.println("Grabbed " + from.name + " lock");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        to.lock.lock();
        System.out.println("Grabbed " + to.name + " lock");
        from.balance -= amount;
        to.balance += amount;
        System.out.println("Transfer amount " + amount
                + " from " + from.name + "->" + to.name + " complete");


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