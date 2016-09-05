package de.pifpafpuf.list;

public class ListNode<T> extends ListElem<T> {
  private ListElem<T> previous = null;
  private ListElem<T> next;
  private final T data;
  public ListNode(T data) {
    this.data = data;
  }
  public T get() {
    return data;
  }
}
