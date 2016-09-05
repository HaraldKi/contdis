package de.pifpafpuf.list;

public abstract class ListElem<T> {
  protected ListElem<T> previous;
  protected ListElem<T> next;
  void append(ListElem<T> e) {
    next = e;
  }
  void prepend(ListElem<T> e) {
    previous = e;
  }
  
  void unlink() {
    previous.next = next;
    next.previous = previous;
    next = null;
    previous = null;
  }
}
