package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by hanqing.tan on 2016/3/25.
 */
public class NIOClient implements Runnable{
    private static final int TIMEOUT = 1000;
    private static final String UTF_8 = "UTF-8";
    private static CharsetEncoder encoder = Charset.forName(UTF_8).newEncoder();
    private static CharsetDecoder decoder = Charset.forName(UTF_8).newDecoder();

    private BlockingQueue<String> words = new ArrayBlockingQueue<String>(5);

    private SocketChannel clientChannel = null;
    private Selector clientSelector = null;

    private void init() {
        try {
            words.put("hi");
            words.put("who");
            words.put("what");
            words.put("where");
            words.put("bye");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        init();

        try {
            clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(8084));
            clientSelector = Selector.open();
            clientChannel.register(clientSelector, SelectionKey.OP_CONNECT);

            while (true) {
                //监听事件,设置每一次监听的超时数
                if (clientSelector.select(TIMEOUT) == 0) {
                    continue;
                }

                Iterator<SelectionKey> ite = clientSelector.selectedKeys().iterator();
                while (ite.hasNext()) {
                    SelectionKey key = ite.next();
                    ite.remove();

                    if (key.isConnectable()) {
                        if (clientChannel.isConnectionPending()) {
                            if (clientChannel.finishConnect()) {
                                //只有当连接成功后才能注册OP_READ事件
                                key.interestOps(SelectionKey.OP_READ);

                                clientChannel.write(encoder.encode(CharBuffer.wrap("begin talking !")));

                                Thread.sleep(1000);
                            } else {
                                key.channel();
                            }
                        }
                    } else if (key.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                        clientChannel.read(byteBuffer);
                        byteBuffer.flip();
                        CharBuffer charBuffer = decoder.decode(byteBuffer);
                        String answer = charBuffer.toString();
                        System.out.println(Thread.currentThread().getId() + "---" + answer);

                        String word = words.poll();
                        if (word != null) {
                            clientChannel.write(encoder.encode(CharBuffer.wrap(word)));
                            Thread.sleep(1000);
                        } else {
                            key.channel();
                            return;
                        }
                    }
                }
            }
        }catch (Exception e) {

        }finally {

            if(clientSelector != null) {
                try {
                    clientSelector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(clientChannel != null) {
                try {
                    clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args)  {
        NIOClient nioClient = new NIOClient();
        nioClient.run();
    }
}
