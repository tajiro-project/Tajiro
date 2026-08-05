import { defineStore } from "pinia";
import { comparisonApi } from "@/api/services";

export const useCompareStore = defineStore("compare", {
  // state: () => ({
  //   items: [],
  //   loaded: false,
  // }),
  // // 비교함 추가
  // actions: {
  //   // async add(property) {
  //   //   try {
  //   //     // 백엔드 API 호출 (백엔드에서 중복 및 3개 초과 검사 진행)
  //   //     await comparisonApi.addToBox(property.propertyId);
  //   //     // 성공 시 로컬 state에 추가
  //   //     this.items.push(property);
  //   //     return { success: true, message: "비교함에 담았어요." };
  //   //   } catch (error) {
  //   //     // 나중에 에러코드 해주면 그때 연결
  //   //     const status = error.response?.status;
  //   //     let msg = "비교함 담기에 실패했습니다.";
  //   //     return { success: false, message: msg };
  //   //   }
  //   // },
  // },
});
