################################################################################
# Automatically-generated file. Do not edit!
# Toolchain: GNU Tools for STM32 (12.3.rel1)
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Core/Src/acc_hal_integration_stm32cube_xe121_multi_sensor.c \
../Core/Src/acc_integration_log.c \
../Core/Src/acc_integration_stm32.c \
../Core/Src/app_debug.c \
../Core/Src/app_entry.c \
../Core/Src/cJSON.c \
../Core/Src/example_detector_distance.c \
../Core/Src/example_detector_presence.c \
../Core/Src/example_service.c \
../Core/Src/hw_timerserver.c \
../Core/Src/main.c \
../Core/Src/stm32_lpm_if.c \
../Core/Src/stm32wbxx_hal_msp.c \
../Core/Src/stm32wbxx_it.c \
../Core/Src/syscalls.c \
../Core/Src/sysmem.c \
../Core/Src/system_stm32wbxx.c 

OBJS += \
./Core/Src/acc_hal_integration_stm32cube_xe121_multi_sensor.o \
./Core/Src/acc_integration_log.o \
./Core/Src/acc_integration_stm32.o \
./Core/Src/app_debug.o \
./Core/Src/app_entry.o \
./Core/Src/cJSON.o \
./Core/Src/example_detector_distance.o \
./Core/Src/example_detector_presence.o \
./Core/Src/example_service.o \
./Core/Src/hw_timerserver.o \
./Core/Src/main.o \
./Core/Src/stm32_lpm_if.o \
./Core/Src/stm32wbxx_hal_msp.o \
./Core/Src/stm32wbxx_it.o \
./Core/Src/syscalls.o \
./Core/Src/sysmem.o \
./Core/Src/system_stm32wbxx.o 

C_DEPS += \
./Core/Src/acc_hal_integration_stm32cube_xe121_multi_sensor.d \
./Core/Src/acc_integration_log.d \
./Core/Src/acc_integration_stm32.d \
./Core/Src/app_debug.d \
./Core/Src/app_entry.d \
./Core/Src/cJSON.d \
./Core/Src/example_detector_distance.d \
./Core/Src/example_detector_presence.d \
./Core/Src/example_service.d \
./Core/Src/hw_timerserver.d \
./Core/Src/main.d \
./Core/Src/stm32_lpm_if.d \
./Core/Src/stm32wbxx_hal_msp.d \
./Core/Src/stm32wbxx_it.d \
./Core/Src/syscalls.d \
./Core/Src/sysmem.d \
./Core/Src/system_stm32wbxx.d 


# Each subdirectory must supply rules for building sources it contributes
Core/Src/%.o Core/Src/%.su Core/Src/%.cyclo: ../Core/Src/%.c Core/Src/subdir.mk
	arm-none-eabi-gcc "$<" -mcpu=cortex-m4 -std=gnu99 -g3 -DDEBUG -DUSE_HAL_DRIVER -DSTM32WB55xx -c -I../Core/Inc -I../Drivers/STM32WBxx_HAL_Driver/Inc -I../Drivers/STM32WBxx_HAL_Driver/Inc/Legacy -I../Drivers/CMSIS/Device/ST/STM32WBxx/Include -I../Drivers/CMSIS/Include -I"D:/stm/work_here/original_board_test/cortex_m4_gcc/rss/include" -I"D:/stm/work_here/original_board_test/cortex_m4_gcc/integration" -I"D:/stm/work_here/original_board_test/cortex_m4_gcc/examples" -I../STM32_WPAN/App -I../Utilities/lpm/tiny_lpm -I../Middlewares/ST/STM32_WPAN -I../Middlewares/ST/STM32_WPAN/interface/patterns/ble_thread -I../Middlewares/ST/STM32_WPAN/interface/patterns/ble_thread/tl -I../Middlewares/ST/STM32_WPAN/interface/patterns/ble_thread/shci -I../Middlewares/ST/STM32_WPAN/utilities -I../Middlewares/ST/STM32_WPAN/ble/core -I../Middlewares/ST/STM32_WPAN/ble/core/auto -I../Middlewares/ST/STM32_WPAN/ble/core/template -I../Middlewares/ST/STM32_WPAN/ble/svc/Inc -I../Middlewares/ST/STM32_WPAN/ble/svc/Src -I../Utilities/sequencer -I../Middlewares/ST/STM32_WPAN/ble -O0 -ffunction-sections -fdata-sections -Wall -fstack-usage -fcyclomatic-complexity -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" --specs=nano.specs -mfpu=fpv4-sp-d16 -mfloat-abi=hard -mthumb -o "$@"

clean: clean-Core-2f-Src

clean-Core-2f-Src:
	-$(RM) ./Core/Src/acc_hal_integration_stm32cube_xe121_multi_sensor.cyclo ./Core/Src/acc_hal_integration_stm32cube_xe121_multi_sensor.d ./Core/Src/acc_hal_integration_stm32cube_xe121_multi_sensor.o ./Core/Src/acc_hal_integration_stm32cube_xe121_multi_sensor.su ./Core/Src/acc_integration_log.cyclo ./Core/Src/acc_integration_log.d ./Core/Src/acc_integration_log.o ./Core/Src/acc_integration_log.su ./Core/Src/acc_integration_stm32.cyclo ./Core/Src/acc_integration_stm32.d ./Core/Src/acc_integration_stm32.o ./Core/Src/acc_integration_stm32.su ./Core/Src/app_debug.cyclo ./Core/Src/app_debug.d ./Core/Src/app_debug.o ./Core/Src/app_debug.su ./Core/Src/app_entry.cyclo ./Core/Src/app_entry.d ./Core/Src/app_entry.o ./Core/Src/app_entry.su ./Core/Src/cJSON.cyclo ./Core/Src/cJSON.d ./Core/Src/cJSON.o ./Core/Src/cJSON.su ./Core/Src/example_detector_distance.cyclo ./Core/Src/example_detector_distance.d ./Core/Src/example_detector_distance.o ./Core/Src/example_detector_distance.su ./Core/Src/example_detector_presence.cyclo ./Core/Src/example_detector_presence.d ./Core/Src/example_detector_presence.o ./Core/Src/example_detector_presence.su ./Core/Src/example_service.cyclo ./Core/Src/example_service.d ./Core/Src/example_service.o ./Core/Src/example_service.su ./Core/Src/hw_timerserver.cyclo ./Core/Src/hw_timerserver.d ./Core/Src/hw_timerserver.o ./Core/Src/hw_timerserver.su ./Core/Src/main.cyclo ./Core/Src/main.d ./Core/Src/main.o ./Core/Src/main.su ./Core/Src/stm32_lpm_if.cyclo ./Core/Src/stm32_lpm_if.d ./Core/Src/stm32_lpm_if.o ./Core/Src/stm32_lpm_if.su ./Core/Src/stm32wbxx_hal_msp.cyclo ./Core/Src/stm32wbxx_hal_msp.d ./Core/Src/stm32wbxx_hal_msp.o ./Core/Src/stm32wbxx_hal_msp.su ./Core/Src/stm32wbxx_it.cyclo ./Core/Src/stm32wbxx_it.d ./Core/Src/stm32wbxx_it.o ./Core/Src/stm32wbxx_it.su ./Core/Src/syscalls.cyclo ./Core/Src/syscalls.d ./Core/Src/syscalls.o ./Core/Src/syscalls.su ./Core/Src/sysmem.cyclo ./Core/Src/sysmem.d ./Core/Src/sysmem.o ./Core/Src/sysmem.su ./Core/Src/system_stm32wbxx.cyclo ./Core/Src/system_stm32wbxx.d ./Core/Src/system_stm32wbxx.o ./Core/Src/system_stm32wbxx.su

.PHONY: clean-Core-2f-Src

