package de.lmu.cis.pocoweb;

class Main {
  public static void main(String[] args) {
    try {
      Client client = Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                                   "pocoweb123");
      System.out.println("sid: " + client.getSid());
      BooksData bs = client.getBooks();
      for (BookData b : bs.books) {
        System.out.println(b.author + " " + b.title + " " + b.bookId);
      }
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
