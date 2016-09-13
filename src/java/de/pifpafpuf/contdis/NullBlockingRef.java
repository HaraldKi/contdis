/**
 * (c) Copyright 2000-2016 Raytion GmbH, Duesseldorf.
 *
 * Der Code inklusive aller Nutzungs- und Verwertungsrechte
 * ist Eigentum der Raytion GmbH. Verbreitung und Nutzung
 * jedweder Art ist untersagt sofern nicht explizit
 * vertraglich festgelegt.
 */

package de.pifpafpuf.contdis;

/**
 * holds a value that can only be gotten if non-{@code null}. If it is
 * {@code null} the getter blocks until a value is available.
 * 
 */
public class NullBlockingRef<V> {
  private volatile V value;
  
  public NullBlockingRef() {
    this(null);
  }
  
  public NullBlockingRef(V value) {
    this.value = value;
  }
  
  public synchronized void set(V value) {
    this.value = value;
    notifyAll();
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
}
