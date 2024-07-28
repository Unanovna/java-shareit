package org.example.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {

    public static Pageable getPageRequest(int from, int size) {
        return PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "id"));
    }
}
