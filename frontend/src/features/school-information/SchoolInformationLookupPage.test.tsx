import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { SchoolInformationLookupPage } from "./SchoolInformationLookupPage";

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("SchoolInformationLookupPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders returned school rows and uses the rendered row count", async () => {
    const fetchMock = vi.fn().mockImplementation(() =>
      response([
        {
          educationOfficeName: "서울특별시교육청",
          schoolName: "가락초등학교",
          schoolTypeName: "초등학교",
          locationName: "송파구",
          foundationName: "공립",
          roadAddress: "서울 송파구 가락로 1",
          telephoneNumber: "02-0000-0000",
        },
      ]),
    );
    vi.stubGlobal("fetch", fetchMock);

    render(<SchoolInformationLookupPage />);
    fireEvent.change(screen.getByLabelText("학교명"), {
      target: { value: "가락" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    expect(await screen.findByText("가락초등학교")).toBeInTheDocument();
    expect(screen.getByText("조회 결과 1건")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/external-integrations/schools?schoolName=%EA%B0%80%EB%9D%BD",
      expect.any(Object),
    );
  });

  it("shows the empty notice without an error banner", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(() => response([])),
    );
    render(<SchoolInformationLookupPage />);

    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    expect(await screen.findByText("조회 결과가 없습니다")).toBeInTheDocument();
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  it("shows the returned error reason, clears stale rows, and allows retry", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        response([
          {
            educationOfficeName: "서울특별시교육청",
            schoolName: "가락초등학교",
            schoolTypeName: "초등학교",
            locationName: "송파구",
            foundationName: "공립",
            roadAddress: "서울 송파구 가락로 1",
            telephoneNumber: "02-0000-0000",
          },
        ]),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            success: false,
            error: { message: "외부 연동 시간이 초과되었습니다." },
            meta: {},
          }),
          { status: 500 },
        ),
      )
      .mockImplementationOnce(() => response([]));
    vi.stubGlobal("fetch", fetchMock);
    render(<SchoolInformationLookupPage />);

    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("가락초등학교");
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "외부 연동 시간이 초과되었습니다.",
    );
    expect(screen.queryByText("가락초등학교")).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "다시 시도" }));
    expect(await screen.findByText("조회 결과가 없습니다")).toBeInTheDocument();
  });
});
