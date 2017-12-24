
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by emmanual on 12/17/17.
 */
public class PageRank {
    private static final double MAX_DELTA = 0.0001;                                                                         //Given
    private static final double BETA = 0.8;                                                                                 //Given

    public static void main(String[] args) {
        System.out.println("Starting Page Rank Analysis......");

                                                                                                                            // Check argument format
        if (args.length != 2) {
            System.out.println("Incorrect Format used");
            System.exit(1);
        }

                                                                                                                            // Input URL information into an ArrayList for easy access
        List<PageNode> PageNodes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line = br.readLine();
            int index = 0;
            while (line != null) {
                PageNodes.add(new PageNode(line, index++));
                line = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("URL reading error: " + e);
            System.exit(1);
        }


        try (BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            String line = br.readLine();
            while (line != null) {                                                                                          //Split method to parse URL links
                String[] split = line.split("\\s+");
                PageNodes.get(Integer.parseInt(split[0])).outLinks.add(Integer.parseInt(split[1]));
                line = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("Error in reading links: " + e);
            System.exit(1);
        }

        System.out.println("Setting up the matrix...");
                                                                                                                            // Initialize variables and matrix
        final int Page = PageNodes.size();
        final double TELEPORT = (1 - BETA) / Page;
        double[] ranks = new double[Page];                                                                                  //ranks vector (r)
        double[][] matrix = new double[Page][Page];                                                                         //M matrix

        Arrays.fill(ranks, 1.0 / Page);                                                                                     //Noramlizing r so all page entries sum equal 1
        for (double[] m : matrix) {
            Arrays.fill(m, TELEPORT);
        }

        int outDegree;
        for (int row = 0; row < Page; row++) {
            for (int col = 0; col < Page; col++) {
                outDegree = PageNodes.get(col).outLinks.size();
                matrix[row][col] += (PageNodes.get(col).outLinks.contains(row)) ? BETA / outDegree : 0;
            }
        }

        System.out.println("Calculating Page Rank...");
        // Matrix calculations
        for (int i = 0; i < 100; i++) {
            double[] rOld = ranks.clone();
            ranks = multiplyMatrices(matrix, ranks);                                                                        //utilize helper method to do matrix multiplication

            double SumOfRanks = DoubleStream.of(ranks).sum();
            for (int r = 0; r < Page; r++) {
                ranks[r] /= SumOfRanks;
            }
                                                                                                                            // distance between r and rOld - the square root of the sum of the squares differences
            double L2 = 0;
            for (int j = 0; j < Page; j++) {
                double diff = ranks[j] - rOld[j];
                diff *= diff;
                L2 += diff;
            }

            if (Math.sqrt(L2) < MAX_DELTA) break;
        }

                                                                                                                            // sort final ranks in decreasing order
        for (PageNode page : PageNodes) {
            page.rankDouble = ranks[page.fileIndex];
        }

        Collections.sort(PageNodes);

        // Print results
        System.out.println("---------Algorithm Page Rank Results--------------");
        for (int l = 0; l < 10 && l < Page; l++) {
            System.out.printf("%-2d: %-45s %.16f\n", l + 1, PageNodes.get(l).URLLink, PageNodes.get(l).rankDouble);
        }
    }


    private static double[] multiplyMatrices(double[][] matrix, double[] vector) {                                          //Matrix and Vector computation
        return Arrays.stream(matrix)
                .mapToDouble(row ->
                        IntStream.range(0, row.length)
                                .mapToDouble(col -> row[col] * vector[col])
                                .sum()
                ).toArray();
    }


    static class PageNode implements Comparable<PageNode> {                                                                 // innerClass
        String URLLink;
        int fileIndex;
        ArrayList<Integer> outLinks;
        double rankDouble;

        PageNode(String URLLink, int fileIndex) {
            this.URLLink = URLLink;
            this.fileIndex = fileIndex;
            outLinks = new ArrayList<>();

        }

        @Override
        public int compareTo(PageNode otherNode) {
            return Double.compare(this.rankDouble, otherNode.rankDouble);
        }
    }

}
