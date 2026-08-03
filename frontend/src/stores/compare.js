import { defineStore } from "pinia";
import { comparisonApi } from "@/api/services";

export const useCompareStore = defineStore("compare", {
  state: () => ({
    items: [], // 비교함 매물 (최대 3개)
    loaded: false,
  }),
  actions: {
    async load() {
      this.items = (await comparisonApi.box()) ?? [];
      this.loaded = true;
    },
    async add(property) {
      if (this.items.length >= 3) return false; // 최대 3개 — 경고 창 표시용
      if (this.items.some((i) => i.propertyId === property.propertyId))
        return false;
      const ok = await comparisonApi.addToBox(property.propertyId);
      if (ok === false) return false;
      this.items.push(property);
      return true;
    },
    async remove(propertyId) {
      await comparisonApi.removeFromBox(propertyId);
      this.items = this.items.filter((i) => i.propertyId !== propertyId);
    },
  },
});
