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
      BookData b = client.getBook(305);
      for (int p : b.pageIds) {
        System.out.println("book " + 305 + " page " + p);
        // TokensData ts = client.getTokens(305, p);
        // for (
      }
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
