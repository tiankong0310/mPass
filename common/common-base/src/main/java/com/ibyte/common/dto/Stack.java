package com.ibyte.common.dto;

import lombok.*;

/**
 * @Description: <Stack>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 21:25
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stack {

    String app;

    String code;

    String message;
}
