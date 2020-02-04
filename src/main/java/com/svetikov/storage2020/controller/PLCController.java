package com.svetikov.storage2020.controller;

import com.svetikov.storage2020.component.PLCComponent;
import com.svetikov.storage2020.models.BoardBox;
import com.svetikov.storage2020.models.PLCData;
import com.svetikov.storage2020.models.PLCDbData;
import com.svetikov.storage2020.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@RestController

@RequestMapping("/app")
public class PLCController {

    private final ModelService<PLCData, Integer> plcService;

    private final ModelService<BoardBox, Long> boardService;

    private final PLCComponent plcComponent;

    private final ModelService<PLCDbData, Integer> plcDbService;

    private List<PLCDbData> plcDbDataSet;
    private PLCData plcData1;

    @Autowired
    public PLCController(@Qualifier("plc") ModelService<PLCData, Integer> plcRepository,
                         @Qualifier("board") ModelService<BoardBox, Long> boardRepository,
                         PLCComponent plcComponent,
                         @Qualifier("db") ModelService plcDbDataIntegerModelService) {
        this.plcService = plcRepository;
        this.boardService = boardRepository;
        this.plcComponent = plcComponent;
        this.plcDbService = plcDbDataIntegerModelService;
    }

//    @GetMapping("/1")
//    public List<PLCData> getA() {
//        return plcRepository.getAllModel();
//    }
//
//    @GetMapping("/2")
//    public void getB() throws Exception {
//        plcComponent.onInitDBAreaPLC();
//    }
//
//    @GetMapping("/3")
//    public List<BoardBox> box() {
//
//        return boardRepository.getAllModel();
//    }
//    @GetMapping("/5")
//    public ResponseEntity< PLCData> getOne(){
//        PLCData plc=new PLCData("test","1",9,1,2,3,4,5);
//        return new ResponseEntity<>(plc,HttpStatus.OK);
//    }

    @PostMapping("/plc/newplc")
    public ResponseEntity<List<PLCData>> newPLCData(@RequestBody PLCData plcData) {

     //   if(!plcDbDataSet.isEmpty())plcDbDataSet.clear();

        log.info("plcData " + plcData.toString());

        if (plcData.getAdrIP().equals(null) & plcData.getDbRead() == 0)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        log.info("!plcService.getAllModel().isEmpty() " + plcService.getAllModel().isEmpty());
        if (!plcService.getAllModel().isEmpty()) {


                plcData1 = plcService.getAllModel().stream()
                        .filter(plc -> plc.getPlcName().equals(plcData.getPlcName()))
                        .findFirst()
                        .orElse(new PLCData("","",-1,-1,-1,
                                -1,-1,-1,null));
//String plcName=plcService.getAllModel().stream()
//        .filter(plc->plc.getPlcName().equals(plcData.getPlcName()))
//        .map(PLCData::getPlcName)
//        .findAny()
//        .orElse("");





            if (plcData1.getPlcName().equals(plcData.getPlcName())) {
//            if(plcName.equals(plcData.getPlcName())){

                plcService.deleteModel(plcData1.getId());
            }
        }

        log.info("!plcDbService.getAllModel().isEmpty() " + plcDbService.getAllModel().isEmpty());

        if (!plcDbService.getAllModel().isEmpty()) {

            plcDbDataSet = plcDbService.getAllModel().stream()
                    .filter(db -> db.getPlcName().equals(plcData.getPlcName()))
                    .collect(Collectors.toList());

            if (!plcDbDataSet.isEmpty()) {
                log.info("!plcDbDataSet.isEmpty() " + plcDbDataSet.isEmpty());
                plcData.setPlcDbData(plcDbDataSet);


            } else {
                plcData.setPlcDbData(null);
            }
        }

        plcService.saveModel(plcData);


        return new ResponseEntity<>(plcService.getAllModel(), HttpStatus.OK);
    }

    @GetMapping("/plc/all")
    public ResponseEntity<List<PLCData>> allPLCs() {
        return new ResponseEntity<>(plcService.getAllModel(), HttpStatus.OK);
    }

    @GetMapping("/plc/delete/{id}")
    public ResponseEntity<List<PLCData>> deletePLC(@PathVariable("id") int id) {
        PLCData plcData = plcService.getModelBuID(id);
        plcService.deleteModel(id);
        return new ResponseEntity<>(plcService.getAllModel(), HttpStatus.OK);
    }

    @GetMapping("/plc/connect/{id}")
    public ResponseEntity<Boolean> plcConnect(@PathVariable("id") int id) throws Exception {
        log.info("connect plc");
        plcComponent.onInitPLC();
        return new ResponseEntity<>(plcComponent.getPlc().connected, HttpStatus.OK);
    }

}
