package de.pifpafpuf.list;

public class ListQueue<T> extends ListElem<T>{

  public ListQueue() {
    super();
    previous = this;
    next = this;
  }
  public void add(T data) {
    add(new ListNode<T>(data));
  }
  
  public void add(ListElem<T> node) {
    tail().append(node);
    node.prepend(this);
  }
  private ListElem<T> tail() {
    return next;
  }
  private ListElem<T> head() {
    return previous;
  }

  public ListElem<T> take() {
    if (this==head()) {
      return null;
    }
    
    ListElem<T> head = head();
    head.unlink();
    return head;
  }
}
