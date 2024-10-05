/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file    App/custom_app.c
  * @author  MCD Application Team
  * @brief   Custom Example Application (Server)
  ******************************************************************************
  * @attention
  *
  * Copyright (c) 2024 STMicroelectronics.
  * All rights reserved.
  *
  * This software is licensed under terms that can be found in the LICENSE file
  * in the root directory of this software component.
  * If no LICENSE file comes with this software, it is provided AS-IS.
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include "app_common.h"
#include "dbg_trace.h"
#include "ble.h"
#include "custom_app.h"
#include "custom_stm.h"
#include "stm32_seq.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */

/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
typedef struct
{
  /* mySvc */
  uint8_t               Mycharnotify_Notification_Status;
  /* USER CODE BEGIN CUSTOM_APP_Context_t */

  /* USER CODE END CUSTOM_APP_Context_t */

  uint16_t              ConnectionHandle;
} Custom_App_Context_t;

/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private defines ------------------------------------------------------------*/
/* USER CODE BEGIN PD */

/* USER CODE END PD */

/* Private macros -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
/**
 * START of Section BLE_APP_CONTEXT
 */

static Custom_App_Context_t Custom_App_Context;

/**
 * END of Section BLE_APP_CONTEXT
 */

uint8_t UpdateCharData[512];
uint8_t NotifyCharData[512];
uint16_t Connection_Handle;
/* USER CODE BEGIN PV */
int a=0;
int tr=1;
/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
/* mySvc */
static void Custom_Mycharnotify_Update_Char(void);
static void Custom_Mycharnotify_Send_Notification(void);

/* USER CODE BEGIN PFP */
void myTask(void)
{
  if(1)
   {
// 	  UpdateCharData[0] ^= 0x1;
	  if(acc_example_service(512,UpdateCharData)==EXIT_SUCCESS){
		  Custom_Mycharnotify_Update_Char();
	  }
// 	 acc_example_detector_distance(0,NULL);
// 	acc_example_detector_presence(0,NULL);

// 	  HAL_Delay(1000);
// 	  if(a%1000==0){
// 		 printf("%d\n",a);
// 	  }
// 	  a+=1;
// 	  tr=0;
// 	  for(int i=0;i<512;i++){
// 		 printf("%c\n",UpdateCharData[i]);
// 	  }
   }
   UTIL_SEQ_SetTask(1 << CFG_TASK_MY_TASK, CFG_SCH_PRIO_0);
}
/* USER CODE END PFP */

/* Functions Definition ------------------------------------------------------*/
void Custom_STM_App_Notification(Custom_STM_App_Notification_evt_t *pNotification)
{
  /* USER CODE BEGIN CUSTOM_STM_App_Notification_1 */

  /* USER CODE END CUSTOM_STM_App_Notification_1 */
  switch (pNotification->Custom_Evt_Opcode)
  {
    /* USER CODE BEGIN CUSTOM_STM_App_Notification_Custom_Evt_Opcode */

    /* USER CODE END CUSTOM_STM_App_Notification_Custom_Evt_Opcode */

    /* mySvc */
    case CUSTOM_STM_MYCHARWRITE_WRITE_EVT:
      /* USER CODE BEGIN CUSTOM_STM_MYCHARWRITE_WRITE_EVT */

      /* USER CODE END CUSTOM_STM_MYCHARWRITE_WRITE_EVT */
      break;

    case CUSTOM_STM_MYCHARNOTIFY_WRITE_NO_RESP_EVT:
      /* USER CODE BEGIN CUSTOM_STM_MYCHARNOTIFY_WRITE_NO_RESP_EVT */

      /* USER CODE END CUSTOM_STM_MYCHARNOTIFY_WRITE_NO_RESP_EVT */
      break;

    case CUSTOM_STM_MYCHARNOTIFY_NOTIFY_ENABLED_EVT:
      /* USER CODE BEGIN CUSTOM_STM_MYCHARNOTIFY_NOTIFY_ENABLED_EVT */

      /* USER CODE END CUSTOM_STM_MYCHARNOTIFY_NOTIFY_ENABLED_EVT */
      break;

    case CUSTOM_STM_MYCHARNOTIFY_NOTIFY_DISABLED_EVT:
      /* USER CODE BEGIN CUSTOM_STM_MYCHARNOTIFY_NOTIFY_DISABLED_EVT */

      /* USER CODE END CUSTOM_STM_MYCHARNOTIFY_NOTIFY_DISABLED_EVT */
      break;

    case CUSTOM_STM_NOTIFICATION_COMPLETE_EVT:
      /* USER CODE BEGIN CUSTOM_STM_NOTIFICATION_COMPLETE_EVT */

      /* USER CODE END CUSTOM_STM_NOTIFICATION_COMPLETE_EVT */
      break;

    default:
      /* USER CODE BEGIN CUSTOM_STM_App_Notification_default */

      /* USER CODE END CUSTOM_STM_App_Notification_default */
      break;
  }
  /* USER CODE BEGIN CUSTOM_STM_App_Notification_2 */

  /* USER CODE END CUSTOM_STM_App_Notification_2 */
  return;
}

void Custom_APP_Notification(Custom_App_ConnHandle_Not_evt_t *pNotification)
{
  /* USER CODE BEGIN CUSTOM_APP_Notification_1 */

  /* USER CODE END CUSTOM_APP_Notification_1 */

  switch (pNotification->Custom_Evt_Opcode)
  {
    /* USER CODE BEGIN CUSTOM_APP_Notification_Custom_Evt_Opcode */

    /* USER CODE END P2PS_CUSTOM_Notification_Custom_Evt_Opcode */
    case CUSTOM_CONN_HANDLE_EVT :
      /* USER CODE BEGIN CUSTOM_CONN_HANDLE_EVT */

      /* USER CODE END CUSTOM_CONN_HANDLE_EVT */
      break;

    case CUSTOM_DISCON_HANDLE_EVT :
      /* USER CODE BEGIN CUSTOM_DISCON_HANDLE_EVT */

      /* USER CODE END CUSTOM_DISCON_HANDLE_EVT */
      break;

    default:
      /* USER CODE BEGIN CUSTOM_APP_Notification_default */

      /* USER CODE END CUSTOM_APP_Notification_default */
      break;
  }

  /* USER CODE BEGIN CUSTOM_APP_Notification_2 */

  /* USER CODE END CUSTOM_APP_Notification_2 */

  return;
}

void Custom_APP_Init(void)
{
  /* USER CODE BEGIN CUSTOM_APP_Init */

  /* USER CODE END CUSTOM_APP_Init */
  return;
}

/* USER CODE BEGIN FD */

/* USER CODE END FD */

/*************************************************************
 *
 * LOCAL FUNCTIONS
 *
 *************************************************************/

/* mySvc */
__USED void Custom_Mycharnotify_Update_Char(void) /* Property Read */
{
  uint8_t updateflag = 0;

  /* USER CODE BEGIN Mycharnotify_UC_1*/
  updateflag = 1;

  /* USER CODE END Mycharnotify_UC_1*/

  if (updateflag != 0)
  {
	Custom_STM_App_Update_Char_Ext(Connection_Handle, CUSTOM_STM_MYCHARNOTIFY, (uint8_t *)UpdateCharData);
  }

  /* USER CODE BEGIN Mycharnotify_UC_Last*/

  /* USER CODE END Mycharnotify_UC_Last*/
  return;
}

void Custom_Mycharnotify_Send_Notification(void) /* Property Notification */
{
  uint8_t updateflag = 0;

  /* USER CODE BEGIN Mycharnotify_NS_1*/

  /* USER CODE END Mycharnotify_NS_1*/

  if (updateflag != 0)
  {
	Custom_STM_App_Update_Char_Ext(Connection_Handle, CUSTOM_STM_MYCHARNOTIFY, (uint8_t *)NotifyCharData);
  }

  /* USER CODE BEGIN Mycharnotify_NS_Last*/

  /* USER CODE END Mycharnotify_NS_Last*/

  return;
}

/* USER CODE BEGIN FD_LOCAL_FUNCTIONS*/

/* USER CODE END FD_LOCAL_FUNCTIONS*/
