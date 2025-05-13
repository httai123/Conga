package com.viettel.vds.cdp.translator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vds.cdp.translator.adaptors.AdaptorISQLTranslator;
import com.viettel.vds.cdp.translator.controllers.Controller;
import com.viettel.vds.entities.cdp.Ruleset;
import com.viettel.vds.model.mongo.cdp.RulesetOperators;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final Path rsPath = Paths.get(
            System.getProperty("user.dir"),
            "src",
            "main",
            "resources"
    );

    Logger logger = Logger.getLogger(getClass().getName());

    public static void main(String[] args) throws Exception {
        String jsonOp = new String(Files.readAllBytes(Paths.get(rsPath.toString(), "operator.json")), StandardCharsets.US_ASCII);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RulesetOperators[] operators = mapper.readValue(jsonOp, RulesetOperators[].class);
        Map<String, RulesetOperators> mapOp = Arrays.stream(operators)
                .collect(Collectors.toMap(
                        RulesetOperators::getCode,
                        x -> x,
                        (existing, replacement) -> replacement // Ghi đè giá trị cũ bằng giá trị mới
                ));


        Path testCaseDir = Paths.get(rsPath.toString(), "test_case_real");
        try (Stream<Path> paths = Files.list(testCaseDir)) {
            paths.filter(Files::isRegularFile)
                    .forEach(pathTest -> {
                        try {
                            String json = new String(Files.readAllBytes(pathTest), StandardCharsets.US_ASCII);
                            Ruleset input = mapper.readValue(json, Ruleset.class);
                            // Run
                            Controller controller = new Controller();
                            AdaptorISQLTranslator adaptor = new AdaptorISQLTranslator(controller, mapOp);
                            String sql = adaptor.translateAndDraw(input);
                            // Write
                            Path outputPath = Paths.get(
                                    rsPath.toString(),
                                    "results_final",
                                    pathTest.getFileName().toString().replace(".json", ".sql")
                            );
                            Files.write(outputPath, sql.getBytes(StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            // print file name error
                            System.out.println(pathTest.getFileName());
                            e.printStackTrace();
                        }
                    });
        }
    }
}