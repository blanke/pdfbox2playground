package edu.ist.psu.sagnik.research.pdfbox2playground.javatest;

/**
 * Created by schoudhury on 6/27/16.
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * @author mkl
 */
public class ExtractText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");


    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/32014589/how-to-read-data-from-table-structured-pdf-using-itextsharp">
     * How to read data from table-structured PDF using itextsharp?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/jwsuu6mz9ez84ss/sampleFile.pdf?dl=0">
     * sampleFile.pdf
     * </a>
     * <p>
     * The extraction behavior of PDFBox as used here, i.e. with <code>SortByPosition</code>
     * being <code>false</code>, can be emulated with iText(Sharp) by explicitly using the
     * </p>
     */

    public void testExtractFromSampleFile() throws IOException {
        try (InputStream documentStream = getClass().getResourceAsStream("sampleFile.pdf");
             PDDocument document = PDDocument.load(documentStream)) {
            String normal = extractNormal(document);

            System.out.println("\n'sampleFile.pdf', extract normally:");
            System.out.println(normal);
            System.out.println("***********************************");
        }
    }

    // extract WITHOUT SortByPosition
    String extractNormal(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        //stripper.setSortByPosition(true);
        return stripper.getText(document);
    }

    /**
     * <a href="http://stackoverflow.com/questions/32978179/using-pdfbox-to-get-location-of-line-of-text">
     * Using PDFBox to get location of line of text
     * </a>
     * <p>
     * This example shows how to extract text with the additional information of
     * the x coordinate at the start of line.
     * </p>
     */

    public void testExtractLineStartFromSampleFile() throws IOException {
        try (InputStream documentStream = getClass().getResourceAsStream("sampleFile.pdf");
             PDDocument document = PDDocument.load(documentStream)) {
            String normal = extractLineStart(document);

            System.out.println("\n'sampleFile.pdf', extract with line starts:");
            System.out.println(normal);
            System.out.println("***********************************");
        }
    }

    String extractLineStart(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void startPage(PDPage page) throws IOException {
                startOfLine = true;
                super.startPage(page);
            }

            @Override
            protected void writeLineSeparator() throws IOException {
                startOfLine = true;
                super.writeLineSeparator();
            }

            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                if (startOfLine) {
                    TextPosition firstProsition = textPositions.get(0);
                    writeString(String.format("[%s]", firstProsition.getXDirAdj()));
                    startOfLine = false;
                }
                super.writeString(text, textPositions);
            }

            boolean startOfLine = true;
        };
        //stripper.setSortByPosition(true);
        return stripper.getText(document);
    }

    /**
     * <a href="http://stackoverflow.com/questions/37566288/pdfbox-is-not-giving-right-output">
     * PDFBOX is not giving right output
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/bsm4zgv5v0mvj7v/Airtel.pdf?dl=0">
     * Airtel.pdf
     * </a>
     * <p>
     * Indeed, PDFBox text extraction hardly returns anything. But inspecting the PDF in question
     * makes clear why it is so: Virtually all "text" in the document is not drawn using text
     * drawing instructions but instead by defining the character outlines as paths and filling
     * them. Thus, hardly anything short of OCR will help here.
     * </p>
     */
    public void testExtractAsMudit() throws IOException {
        try (InputStream documentStream = getClass().getResourceAsStream("Airtel.pdf");
             PDDocument document = PDDocument.load(documentStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);
            String parsedText = pdfStripper.getText(document);

            System.out.println("\n'Airtel.pdf', extract as Mudit:");
            System.out.println(parsedText);
            System.out.println("***********************************");
        }
    }
}
