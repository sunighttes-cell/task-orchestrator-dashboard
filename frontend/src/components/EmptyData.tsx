import {Empty, EmptyTitle, EmptyDescription} from "@/components/ui/empty"

export const EmptyData = () => {
  return (
    <>
        <Empty>
            <EmptyTitle>No Jobs Found</EmptyTitle>
            <EmptyDescription>
                Try adjusting your search or filter to find what you're looking for.
            </EmptyDescription>
      </Empty>
    </> 
  );
}