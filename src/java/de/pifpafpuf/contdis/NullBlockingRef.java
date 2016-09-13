package de.pifpafpuf.contdis;

/**
 * holds a value that can only be gotten if non-{@code null}. If it is
 * {@code null} the getter blocks until a value is available.
 * 
 * inspired from http://stackoverflow.com/a/39442283/2954288 
 */
public class NullBlockingRef<V> {
  private volatile V value;
  
  public NullBlockingRef() {
    this(null);
  }
  
  public NullBlockingRef(V value) {
    this.value = value;
  }
  
  /**
   * sets the value and notfies all threads waiting for non-null to take a
   * draw
   */
  public synchronized void set(V value) {
    this.value = value;
    if (value!=null) {
      notifyAll();
    }
  }
  
  /**
   * gets the contained value if non-{@code null}. Blocks while the value is
   * {@code null}. No guarantee is made that every change to non-{@code null} is
   * delivered. The only guarantee is that if the call returns, the result is
   * not {@code null} and is a value that was set or provided to the
   * constructor.
   * @return a non-{@code null} value;
   * @throws InterruptedException
   */
  public synchronized V get() throws InterruptedException {
    V result;
    while (null==(result=value)) {
      wait();
    }
    return result;
  }
  
  public synchronized V getOrNull() {
    return value;
  }
}
