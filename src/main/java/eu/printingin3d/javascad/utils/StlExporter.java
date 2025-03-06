package eu.printingin3d.javascad.utils;

import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.VertexHolder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;


public class StlExporter {

    private static final int X = 0;
    private static final int Y = 0;
    private static final int Z = 0;


    public static void saveStl(VertexHolder holder, String fileName) {
        List<Facet> originalFacets = holder.getFacets(); // Ваши исходные грани

        List<Facet> fixedFacets =  StlValidator.validateAndRepair(originalFacets);

        try (FileChannel channel = new FileOutputStream(fileName).getChannel()) {
            StlExporter.writeBinaryStl(originalFacets, channel);
            System.out.println("Export to " + fileName + " is done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void writeBinaryStl(
        List<Facet> facets,
        WritableByteChannel channel
    ) throws IOException {

        // Заголовок файла (80 байт)
        byte[] header = new byte[80];
        ByteBuffer buffer = ByteBuffer.allocate(84 + 50 * facets.size())
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(header);

        // Количество треугольников (4 байта)
        buffer.putInt(facets.size());

        // Запись каждого треугольника
        for (Facet facet : facets) {
            V3d normal = facet.getNormal();
            Triangle3d triangle = facet.getTriangle();
            List<V3d> points = triangle.getPoints();

            // Нормаль (3 float)
            buffer.putFloat((float) normal.getX());
            buffer.putFloat((float) normal.getY());
            buffer.putFloat((float) normal.getZ());

            // Координаты вершин (3 точки по 3 float)
            for (V3d point : points) {
                buffer.putFloat((float) point.getX());
                buffer.putFloat((float) point.getY());
                buffer.putFloat((float) point.getZ());
            }

            // Атрибуты (2 байта)
            buffer.putShort((short) 0);
        }

        buffer.flip();
        channel.write(buffer);
    }
}
