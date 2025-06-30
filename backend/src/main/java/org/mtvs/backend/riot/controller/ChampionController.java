package org.mtvs.backend.riot.controller;

/*챔피언 조회 컨트롤러*/

import org.mtvs.backend.riot.dto.ChampionDto;
import org.mtvs.backend.riot.service.ChampionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/champions")
public class ChampionController {

    private final ChampionService championService;

    public ChampionController(ChampionService championService) {
        this.championService = championService;
    }

    //챔피언 정보 동기화 API
    @PostMapping("/sync")
    public ResponseEntity<String> syncChampions() {
        try {
            championService.syncChampions();
            return ResponseEntity.ok("챔피언 데이터 동기화 완료");
        }catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("챔피언 데이터 동기화 중 오류 발생" + e.getMessage());
        }
    }

    //전체 챔피언 목록 조회
    @GetMapping
    public ResponseEntity<List<ChampionDto>> getAllChampions(){
        try{
            List<ChampionDto> champions = championService.getAllChampions();
            return ResponseEntity.ok(champions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //특정 챔피언 조회
    @GetMapping("/{id}")
    public ResponseEntity<ChampionDto> getChampionById(@PathVariable String id){
        try{
            ChampionDto champion = championService.getChampionById(id);
            return ResponseEntity.ok(champion);
        }catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
