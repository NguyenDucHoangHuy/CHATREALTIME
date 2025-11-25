package com.hhy.apiserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)  //Cái nào null trong json response thì nó sẽ ẩn cái trường đó
public class ApiResponse<T> {
    int code = 1000;  //1000 thì response thành công
    String message;
    T result;
}
