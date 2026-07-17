package io.jacksoon.console.controller;

import io.jacksoon.console.dto.response.FilterResponseDto;
import io.jacksoon.console.dto.response.ServiceResponseDto;
import io.jacksoon.console.service.ConsoleService;
import io.jacksoon.console.type.FilterTiming;
import io.jacksoon.console.type.PipelineType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ConsoleViewController {

    private final ConsoleService consoleService;

    public ConsoleViewController(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }

    @GetMapping("/")
    public String getServices(Model model) {
        List<ServiceResponseDto> services = consoleService.getServices();
        model.addAttribute("services", services);
        return "index";
    }

    @GetMapping("/filter")
    public String getFilters(Model model) {
        List<FilterResponseDto> filterStages = consoleService.getFilters();
        model.addAttribute("filterStages", filterStages);
        return "filters";
    }

    @GetMapping("/filter/register")
    public String getFilterRegisterForm(Model model) {
        model.addAttribute("pipelines", PipelineType.values());
        model.addAttribute("timings", FilterTiming.values());
        return "filter-register";
    }

    @PostMapping("/filter/upload")
    public String uploadFilterClass(
            @RequestParam("filterName") String filterName,
            @RequestParam("pipeline") PipelineType pipeline,
            @RequestParam("timing") FilterTiming timing,
            @RequestParam("order") int order,
            @RequestParam("classFile") MultipartFile classFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            consoleService.registerFilterClass(filterName, pipeline, timing, order, classFile);
            redirectAttributes.addFlashAttribute("uploadMessage", "필터가 비활성 상태로 등록되었습니다.");
            return "redirect:/filter";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("uploadError", exception.getMessage());
            return "redirect:/filter/register";
        }
    }
}