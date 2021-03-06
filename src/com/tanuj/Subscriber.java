package com.tanuj;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.Base64;

public class Subscriber {
    static String address;
    static final String channel = "slides";

    public Subscriber(String address) {
        /*Jedis jedis = new Jedis("localhost");
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Enter the channel name:");
        String channel = scanner.nextLine();*/

        Jedis jedis = new Jedis(address);
        System.out.println("Starting subscriber for channel " + channel);
        final ImageDisplay display = new ImageDisplay(640, 480);
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    super.onMessage(channel, message);

                    String[] contents = message.split(";");
                    int height = Integer.parseInt(contents[0]);
                    int width = Integer.parseInt(contents[1]);
                    byte [] imageBytes = Base64.getDecoder().decode(contents[2]) ;
//                    int height = Integer.parseInt(message.substring(0, message.indexOf(";")));
//                    message = message.substring(message.indexOf(";")+1);
//                    int width = Integer.parseInt(message.substring(0, message.indexOf(";")));
//                    message = message.substring(message.indexOf(";")+1);
//                    byte [] imageBytes = Base64.getDecoder().decode(message);
                    BufferedImage image = createRGBImage(imageBytes, width, height);

                    display.setImage(image);
                }

                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                }

                @Override
                public void onPMessage(String pattern, String channel, String message) {
                }

                @Override
                public void onPUnsubscribe(String pattern, int subscribedChannels) {
                }

                @Override
                public void onPSubscribe(String pattern, int subscribedChannels) {
                }

            }, channel);

        while (true) {

        }
    }

    private static BufferedImage createRGBImage(byte[] bytes, int width, int height) {
        DataBufferByte buffer = new DataBufferByte(bytes, bytes.length);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8},
                false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        System.out.printf("Byte Length: %d", bytes.length);
        return new BufferedImage(cm, Raster.createInterleavedRaster(buffer, width, height, width * 3,
                3, new int[]{0, 1, 2}, null), false, null);
    }
}