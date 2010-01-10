public class ServerDescriptorDownloader {
  private ServerDescriptorParser sdp;
  private String authority;
  public ServerDescriptorDownloader(ServerDescriptorParser sdp,
      String authority) {
    this.sdp = sdp;
    this.authority = authority;
  }
}

